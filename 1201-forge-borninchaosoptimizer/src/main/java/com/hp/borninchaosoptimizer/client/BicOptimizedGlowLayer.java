package com.hp.borninchaosoptimizer.client;

import com.hp.borninchaosoptimizer.config.BicOptimizerConfig;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.cache.object.GeoCube;
import software.bernie.geckolib.cache.object.GeoQuad;
import software.bernie.geckolib.cache.object.GeoVertex;
import software.bernie.geckolib.cache.texture.AnimatableTexture;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;
import software.bernie.geckolib.util.RenderUtils;

/**
 * 替代生于混沌全部 44 个 GeoRenderLayer 发光层的稀疏合批渲染器：
 * 1. 仅递归遍历在发光贴图上实际存在非零像素的骨骼与面片（平均剔除 90%+ 空白立方体）；
 * 2. 复用工作向量消除每顶点 JOML Vector3f/Vector4f 堆分配；
 * 3. 在世界实体阶段与 BicGlowBatchBuffer 协同延迟合批，消除每只生物触发的 BufferSource.endBatch() 断批开销。
 */
@OnlyIn(Dist.CLIENT)
public final class BicOptimizedGlowLayer<T extends Entity & GeoAnimatable> extends GeoRenderLayer<T> {
    private final GeoEntityRenderer<T> entityRenderer;
    private final ResourceLocation glowTexture;
    private final GeoRenderLayer<T> fallbackLayer;
    private final Vector3f scratchNormal = new Vector3f();
    private final Vector4f scratchPos = new Vector4f();

    public BicOptimizedGlowLayer(GeoEntityRenderer<T> entityRenderer, ResourceLocation glowTexture, GeoRenderLayer<T> fallbackLayer) {
        super(entityRenderer);
        this.entityRenderer = entityRenderer;
        this.glowTexture = glowTexture;
        this.fallbackLayer = fallbackLayer;
    }

    @Override
    public void render(
            PoseStack poseStack,
            T animatable,
            BakedGeoModel bakedModel,
            RenderType renderType,
            MultiBufferSource bufferSource,
            VertexConsumer buffer,
            float partialTick,
            int packedLight,
            int packedOverlay
    ) {
        if (!BicOptimizerConfig.getBoolean(BicOptimizerConfig.CLIENT.enableClientOptimizations)
                || !BicOptimizerConfig.getBoolean(BicOptimizerConfig.CLIENT.optimizeGlowLayers)) {
            this.fallbackLayer.render(poseStack, animatable, bakedModel, renderType, bufferSource, buffer, partialTick, packedLight, packedOverlay);
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        if (animatable.isInvisibleTo(mc.player)) {
            if (mc.shouldEntityAppearGlowing(animatable)) {
                this.fallbackLayer.render(poseStack, animatable, bakedModel, renderType, bufferSource, buffer, partialTick, packedLight, packedOverlay);
            }
            return;
        }

        double maxDist = animatable.getBbHeight() < 0.65f
                ? BicOptimizerConfig.getDouble(BicOptimizerConfig.CLIENT.smallMobGlowMaxDistance)
                : BicOptimizerConfig.getDouble(BicOptimizerConfig.CLIENT.glowLayerMaxDistance);
        if (maxDist > 0.0 && mc.gameRenderer.getMainCamera().getPosition().distanceToSqr(animatable.position()) > maxDist * maxDist) {
            return;
        }

        BakedGeoModel activeModel = bakedModel != null ? bakedModel : this.getDefaultBakedModel(animatable);
        BicClientRenderManager.GlowModelPlan plan = BicClientRenderManager.getGlowPlan(activeModel, this.glowTexture);
        if (plan.isEmpty()) {
            return;
        }

        RenderType glowRenderType = BicClientRenderManager.getCachedEyesRenderType(this.glowTexture);
        boolean deferred = BicGlowBatchBuffer.canBatch(bufferSource, plan.isAnimated());
        BicGlowBatchBuffer.RenderTypeBucket batchBucket = deferred ? BicGlowBatchBuffer.getOrCreateBucket(glowRenderType) : null;
        VertexConsumer directConsumer = deferred ? null : bufferSource.getBuffer(glowRenderType);

        if (!deferred && plan.isAnimated()) {
            AnimatableTexture.setAndUpdate(this.glowTexture);
        }

        @SuppressWarnings("unchecked")
        BicGeoEntityRendererHook<T> hook = (BicGeoEntityRendererHook<T>) this.entityRenderer;
        hook.bic$pushAndApplyEntityPose(poseStack, animatable, partialTick);
        try {
            for (BicClientRenderManager.GlowBoneNode rootNode : plan.rootBones()) {
                this.renderBoneNode(poseStack, rootNode, packedLight, batchBucket, directConsumer);
            }
        } finally {
            poseStack.popPose();
        }
    }

    private void renderBoneNode(
            PoseStack poseStack,
            BicClientRenderManager.GlowBoneNode node,
            int packedLight,
            BicGlowBatchBuffer.RenderTypeBucket batchBucket,
            VertexConsumer directConsumer
    ) {
        GeoBone bone = node.liveBone();
        poseStack.pushPose();
        RenderUtils.prepMatrixForBone(poseStack, bone);
        if (!bone.isHidden()) {
            for (BicClientRenderManager.GlowCubeNode cubeNode : node.cubes()) {
                this.renderCubeNode(poseStack, cubeNode, packedLight, batchBucket, directConsumer);
            }
        }
        if (!bone.isHidingChildren()) {
            for (BicClientRenderManager.GlowBoneNode child : node.children()) {
                this.renderBoneNode(poseStack, child, packedLight, batchBucket, directConsumer);
            }
        }
        poseStack.popPose();
    }

    private void renderCubeNode(
            PoseStack poseStack,
            BicClientRenderManager.GlowCubeNode cubeNode,
            int packedLight,
            BicGlowBatchBuffer.RenderTypeBucket batchBucket,
            VertexConsumer directConsumer
    ) {
        GeoCube cube = cubeNode.cube();
        poseStack.pushPose();
        RenderUtils.translateToPivotPoint(poseStack, cube);
        RenderUtils.rotateMatrixAroundCube(poseStack, cube);
        RenderUtils.translateAwayFromPivotPoint(poseStack, cube);

        PoseStack.Pose lastPose = poseStack.last();
        Matrix3f normalMat = lastPose.normal();
        Matrix4f poseMat = lastPose.pose();
        Vector3f norm = this.scratchNormal;
        Vector4f pos = this.scratchPos;

        for (GeoQuad quad : cubeNode.quads()) {
            norm.set(quad.normal());
            normalMat.transform(norm);
            RenderUtils.fixInvertedFlatCube(cube, norm);
            float nx = norm.x();
            float ny = norm.y();
            float nz = norm.z();

            for (GeoVertex vertex : quad.vertices()) {
                Vector3f vp = vertex.position();
                pos.set(vp.x(), vp.y(), vp.z(), 1.0f);
                poseMat.transform(pos);
                if (batchBucket != null) {
                    batchBucket.pushVertex(pos.x(), pos.y(), pos.z(), vertex.texU(), vertex.texV(), packedLight, nx, ny, nz);
                } else {
                    directConsumer.vertex(
                            pos.x(),
                            pos.y(),
                            pos.z(),
                            1.0f,
                            1.0f,
                            1.0f,
                            1.0f,
                            vertex.texU(),
                            vertex.texV(),
                            OverlayTexture.NO_OVERLAY,
                            packedLight,
                            nx,
                            ny,
                            nz
                    );
                }
            }
        }
        poseStack.popPose();
    }
}
