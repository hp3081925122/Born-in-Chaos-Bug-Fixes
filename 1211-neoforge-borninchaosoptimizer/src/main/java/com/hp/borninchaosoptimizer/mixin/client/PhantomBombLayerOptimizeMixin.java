package com.hp.borninchaosoptimizer.mixin.client;

import com.hp.borninchaosoptimizer.client.ClientRenderCache;
import com.hp.borninchaosoptimizer.config.OptimizerConfig;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.mcreator.borninchaosv.client.model.Modelphantombomb;
import net.mcreator.borninchaosv.entity.PhantomBombEntityEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 修复 PhantomBombEntityRenderer$1 在每帧 render() 中重复调用 bakeLayer(Modelphantombomb.LAYER_LOCATION) 重建网格的问题，
 * 并将其发光层纳入 ClientRenderCache 固定缓冲合批管线。
 */
@Mixin(targets = "net.mcreator.borninchaosv.client.renderer.PhantomBombEntityRenderer$1", remap = false)
public abstract class PhantomBombLayerOptimizeMixin
        extends RenderLayer<PhantomBombEntityEntity, Modelphantombomb<PhantomBombEntityEntity>> {
    @Unique
    private static final ResourceLocation BIC$PHANTOM_BOMB_GLOW_LOC =
            ResourceLocation.fromNamespaceAndPath("born_in_chaos_v1", "textures/entities/phantombomb_e.png");
    @Unique
    private RenderType bic$cachedGlowType;
    @Unique
    private Modelphantombomb<PhantomBombEntityEntity> bic$cachedLayerModel;
    @Unique
    private int bic$cachedEpoch = -1;

    protected PhantomBombLayerOptimizeMixin(
            RenderLayerParent<PhantomBombEntityEntity, Modelphantombomb<PhantomBombEntityEntity>> renderer
    ) {
        super(renderer);
    }

    @Inject(
            method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/mcreator/borninchaosv/entity/PhantomBombEntityEntity;FFFFFF)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void bic$renderCachedPhantomBombGlow(
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int light,
            PhantomBombEntityEntity entity,
            float limbSwing,
            float limbSwingAmount,
            float partialTicks,
            float ageInTicks,
            float netHeadYaw,
            float headPitch,
            CallbackInfo ci
    ) {
        boolean cacheModel = OptimizerConfig.isEnabled(OptimizerConfig.CACHE_CUSTOM_ARMOR_MODELS);
        boolean optGlow = OptimizerConfig.isEnabled(OptimizerConfig.OPTIMIZE_GLOW_LAYERS_AND_RENDER_TYPES);
        if (!cacheModel && !optGlow) {
            return;
        }

        RenderType glowType = this.bic$cachedGlowType;
        if (glowType == null) {
            this.bic$cachedGlowType = glowType = ClientRenderCache.eyes(BIC$PHANTOM_BOMB_GLOW_LOC);
        }
        if (optGlow) {
            ClientRenderCache.markGlowActive(glowType);
        }

        Modelphantombomb<PhantomBombEntityEntity> model;
        if (cacheModel) {
            int epoch = ClientRenderCache.getReloadEpoch();
            model = this.bic$cachedLayerModel;
            if (model == null || this.bic$cachedEpoch != epoch) {
                model = new Modelphantombomb<>(Minecraft.getInstance().getEntityModels().bakeLayer(Modelphantombomb.LAYER_LOCATION));
                this.bic$cachedLayerModel = model;
                this.bic$cachedEpoch = epoch;
            }
        } else {
            model = new Modelphantombomb<>(Minecraft.getInstance().getEntityModels().bakeLayer(Modelphantombomb.LAYER_LOCATION));
        }

        VertexConsumer vertexConsumer = bufferSource.getBuffer(glowType);
        this.getParentModel().copyPropertiesTo(model);
        model.prepareMobModel(entity, limbSwing, limbSwingAmount, partialTicks);
        model.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        model.renderToBuffer(poseStack, vertexConsumer, light, LivingEntityRenderer.getOverlayCoords(entity, 0.0f));

        if (!optGlow && bufferSource instanceof MultiBufferSource.BufferSource bs) {
            bs.endBatch(glowType);
        }
        ci.cancel();
    }
}
