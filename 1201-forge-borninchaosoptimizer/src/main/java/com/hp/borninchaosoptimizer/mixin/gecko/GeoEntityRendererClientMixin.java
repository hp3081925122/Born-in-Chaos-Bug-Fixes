package com.hp.borninchaosoptimizer.mixin.gecko;

import com.hp.borninchaosoptimizer.Borninchaosoptimizer;
import com.hp.borninchaosoptimizer.client.BicClientRenderManager;
import com.hp.borninchaosoptimizer.client.BicGeoEntityRendererHook;
import com.hp.borninchaosoptimizer.client.BicOptimizedGlowLayer;
import com.hp.borninchaosoptimizer.config.BicOptimizerConfig;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

import java.lang.reflect.Field;

/**
 * 针对生于混沌全部 83 个 GeoEntityRenderer 子类的客户端注入：
 * 1. 自动将无半透明像素实体的基础渲染类型由 entityTranslucent 切换为 entityCutoutNoCull；
 * 2. 在注册渲染层时自动将生于混沌的 44 个发光层包装为稀疏合批的 BicOptimizedGlowLayer；
 * 3. 向 BicOptimizedGlowLayer 暴露完全一致的实体姿态矩阵变换。
 */
@Mixin(GeoEntityRenderer.class)
public abstract class GeoEntityRendererClientMixin<T extends Entity & GeoAnimatable> extends EntityRenderer<T> implements GeoRenderer<T>, BicGeoEntityRendererHook<T> {
    @Shadow(remap = false)
    protected T animatable;

    @Shadow(remap = false)
    protected abstract void applyRotations(T animatable, PoseStack poseStack, float ageInTicks, float rotationYaw, float partialTick);

    @Unique
    private boolean bic$isBicRenderer;

    protected GeoEntityRendererClientMixin(EntityRendererProvider.Context context) {
        super(context);
    }

    @Inject(
            method = "<init>(Lnet/minecraft/client/renderer/entity/EntityRendererProvider$Context;Lsoftware/bernie/geckolib/model/GeoModel;)V",
            at = @At("RETURN"),
            remap = false
    )
    private void bic$initBicRendererFlag(EntityRendererProvider.Context renderManager, GeoModel<T> model, CallbackInfo ci) {
        this.bic$isBicRenderer = this.getClass().getName().startsWith("net.mcreator.borninchaosv.");
    }

    @ModifyVariable(
            method = "addRenderLayer",
            at = @At("HEAD"),
            argsOnly = true,
            remap = false
    )
    @SuppressWarnings("unchecked")
    private GeoRenderLayer<T> bic$wrapGlowRenderLayer(GeoRenderLayer<T> renderLayer) {
        if (!this.bic$isBicRenderer || renderLayer == null) {
            return renderLayer;
        }
        Class<?> layerClass = renderLayer.getClass();
        if (!"net.mcreator.borninchaosv.entity.layer".equals(layerClass.getPackageName())) {
            return renderLayer;
        }
        try {
            Field layerField = layerClass.getDeclaredField("LAYER");
            layerField.setAccessible(true);
            Object val = layerField.get(null);
            if (val instanceof ResourceLocation glowTex) {
                return new BicOptimizedGlowLayer<>((GeoEntityRenderer<T>) (Object) this, glowTex, renderLayer);
            }
        } catch (ReflectiveOperationException e) {
            Borninchaosoptimizer.LOGGER.warn("[BornInChaosOptimizer] Failed to inspect LAYER field on {}: {}", layerClass.getName(), e.toString());
        }
        return renderLayer;
    }

    @Inject(
            method = "render(Lnet/minecraft/world/entity/Entity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void bic$renderWithOptimizedRenderType(
            T entity,
            float entityYaw,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight,
            CallbackInfo ci
    ) {
        if (!this.bic$isBicRenderer) {
            return;
        }
        if (!BicOptimizerConfig.getBoolean(BicOptimizerConfig.CLIENT.enableClientOptimizations)
                || !BicOptimizerConfig.getBoolean(BicOptimizerConfig.CLIENT.optimizeOpaqueEntityRenderTypes)) {
            return;
        }
        ci.cancel();
        this.animatable = entity;
        ResourceLocation texture = this.getGeoModel().getTextureResource(entity, this);
        RenderType resolvedType = BicClientRenderManager.resolveEntityRenderType(entity, texture);
        this.defaultRender(poseStack, entity, bufferSource, resolvedType, null, entityYaw, partialTick, packedLight);
    }

    @Override
    public void bic$pushAndApplyEntityPose(PoseStack poseStack, T entity, float partialTick) {
        poseStack.pushPose();
        LivingEntity livingEntity = entity instanceof LivingEntity living ? living : null;
        boolean shouldSit = entity.isPassenger() && entity.getVehicle() != null && entity.getVehicle().shouldRiderSit();
        float lerpBodyRot = livingEntity == null ? 0.0f : Mth.rotLerp(partialTick, livingEntity.yBodyRotO, livingEntity.yBodyRot);
        float lerpHeadRot = livingEntity == null ? 0.0f : Mth.rotLerp(partialTick, livingEntity.yHeadRotO, livingEntity.yHeadRot);
        if (shouldSit && entity.getVehicle() instanceof LivingEntity vehicleLiving) {
            lerpBodyRot = Mth.rotLerp(partialTick, vehicleLiving.yBodyRotO, vehicleLiving.yBodyRot);
            float netHeadYaw = lerpHeadRot - lerpBodyRot;
            float clampedHeadYaw = Mth.clamp(Mth.wrapDegrees(netHeadYaw), -85.0f, 85.0f);
            lerpBodyRot = lerpHeadRot - clampedHeadYaw;
            if (clampedHeadYaw * clampedHeadYaw > 2500.0f) {
                lerpBodyRot += clampedHeadYaw * 0.2f;
            }
        }
        if (entity.getPose() == Pose.SLEEPING && livingEntity != null) {
            Direction bedDirection = livingEntity.getBedOrientation();
            if (bedDirection != null) {
                float eyePosOffset = livingEntity.getEyeHeight(Pose.STANDING) - 0.1f;
                poseStack.translate(-bedDirection.getStepX() * eyePosOffset, 0.0f, -bedDirection.getStepZ() * eyePosOffset);
            }
        }
        float ageInTicks = entity.tickCount + partialTick;
        this.applyRotations(entity, poseStack, ageInTicks, lerpBodyRot, partialTick);
        poseStack.translate(0.0f, 0.01f, 0.0f);
    }
}
