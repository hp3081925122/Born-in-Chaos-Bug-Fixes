package com.hp.borninchaosoptimizer.client;

import com.hp.borninchaosoptimizer.config.BicOptimizerConfig;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.mcreator.borninchaosv.client.model.Modelphantombomb;
import net.mcreator.borninchaosv.entity.PhantomBombEntityEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Objects;

/**
 * 幻影炸弹实体发光层模型缓存实现：
 * 替代原模组匿名内部类每渲染帧重复调用 bakeLayer 重建骨骼网格并新建 Modelphantombomb 的高开销逻辑，
 * 并在 F3+T 资源包重载时自动刷新缓存。
 */
@OnlyIn(Dist.CLIENT)
public final class CachedPhantomBombGlowLayer extends RenderLayer<PhantomBombEntityEntity, Modelphantombomb<PhantomBombEntityEntity>> {
    private static final ResourceLocation GLOW_TEXTURE =
            Objects.requireNonNull(ResourceLocation.tryBuild("born_in_chaos_v1", "textures/entities/phantombomb_e.png"));
    private static volatile int reloadGeneration;

    private EntityModelSet cachedModelSet;
    private int cachedGeneration = -1;
    private Modelphantombomb<PhantomBombEntityEntity> cachedModel;

    public CachedPhantomBombGlowLayer(RenderLayerParent<PhantomBombEntityEntity, Modelphantombomb<PhantomBombEntityEntity>> parent) {
        super(parent);
    }

    public static void markReloaded() {
        reloadGeneration++;
    }

    @Override
    public void render(
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight,
            PhantomBombEntityEntity entity,
            float limbSwing,
            float limbSwingAmount,
            float partialTicks,
            float ageInTicks,
            float netHeadYaw,
            float headPitch
    ) {
        Minecraft mc = Minecraft.getInstance();
        if (entity.isInvisibleTo(mc.player) && !mc.shouldEntityAppearGlowing(entity)) {
            return;
        }
        RenderType eyesType = BicClientRenderManager.getCachedEyesRenderType(GLOW_TEXTURE);
        VertexConsumer consumer = bufferSource.getBuffer(eyesType);

        Modelphantombomb<PhantomBombEntityEntity> model;
        if (BicOptimizerConfig.getBoolean(BicOptimizerConfig.CLIENT.enableClientOptimizations)
                && BicOptimizerConfig.getBoolean(BicOptimizerConfig.CLIENT.cacheHumanoidArmorModels)) {
            EntityModelSet currentSet = mc.getEntityModels();
            int gen = reloadGeneration;
            model = this.cachedModel;
            if (model == null || this.cachedModelSet != currentSet || this.cachedGeneration != gen) {
                model = new Modelphantombomb<>(currentSet.bakeLayer(Modelphantombomb.LAYER_LOCATION));
                this.cachedModel = model;
                this.cachedModelSet = currentSet;
                this.cachedGeneration = gen;
            }
        } else {
            model = new Modelphantombomb<>(mc.getEntityModels().bakeLayer(Modelphantombomb.LAYER_LOCATION));
        }

        this.getParentModel().copyPropertiesTo(model);
        model.prepareMobModel(entity, limbSwing, limbSwingAmount, partialTicks);
        model.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        model.renderToBuffer(poseStack, consumer, packedLight, LivingEntityRenderer.getOverlayCoords(entity, 0.0f), 1.0f, 1.0f, 1.0f, 1.0f);
    }
}
