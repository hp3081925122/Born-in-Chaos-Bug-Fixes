package com.hp.borninchaosoptimizer.mixin.client;

import com.hp.borninchaosoptimizer.client.ClientRenderCache;
import com.hp.borninchaosoptimizer.config.OptimizerConfig;
import net.mcreator.borninchaosv.item.renderer.BonescallerStaffItemRenderer;
import net.mcreator.borninchaosv.item.renderer.PumpkinhandgunItemRenderer;
import net.mcreator.borninchaosv.item.renderer.PumpkinstaffaItemRenderer;
import net.mcreator.borninchaosv.item.renderer.StaffOfMagicArrowsAItemRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * 缓存 4 种 GeckoLib 3D 武器/法杖渲染器的 RenderType.entityTranslucent 实例。
 */
@Mixin(value = {
        BonescallerStaffItemRenderer.class,
        PumpkinhandgunItemRenderer.class,
        PumpkinstaffaItemRenderer.class,
        StaffOfMagicArrowsAItemRenderer.class
}, remap = false)
public abstract class GeoItemRendererOptimizeMixin {
    @Unique
    private ResourceLocation bic$lastItemTex;
    @Unique
    private boolean bic$lastItemOpaqueOpt;
    @Unique
    private int bic$lastItemEpoch = -1;
    @Unique
    private RenderType bic$cachedItemRenderType;

    @Redirect(
            method = "getRenderType*",
            require = 0,
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/RenderType;entityTranslucent(Lnet/minecraft/resources/ResourceLocation;)Lnet/minecraft/client/renderer/RenderType;"
            )
    )
    private RenderType bic$cacheItemTranslucent(ResourceLocation texture) {
        if (!OptimizerConfig.isEnabled(OptimizerConfig.OPTIMIZE_GLOW_LAYERS_AND_RENDER_TYPES)) {
            return RenderType.entityTranslucent(texture);
        }
        boolean opaqueOpt = OptimizerConfig.isEnabled(OptimizerConfig.OPTIMIZE_OPAQUE_ENTITY_RENDER_TYPES);
        int epoch = ClientRenderCache.getReloadEpoch();
        if (texture != null
                && texture == this.bic$lastItemTex
                && opaqueOpt == this.bic$lastItemOpaqueOpt
                && epoch == this.bic$lastItemEpoch
                && this.bic$cachedItemRenderType != null) {
            return this.bic$cachedItemRenderType;
        }
        RenderType resolved = ClientRenderCache.resolveItemRenderType(texture);
        this.bic$lastItemTex = texture;
        this.bic$lastItemOpaqueOpt = opaqueOpt;
        this.bic$lastItemEpoch = epoch;
        this.bic$cachedItemRenderType = resolved;
        return resolved;
    }
}
