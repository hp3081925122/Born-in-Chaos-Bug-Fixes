package com.hp.borninchaosoptimizer.mixin.gecko;

import com.hp.borninchaosoptimizer.client.BicClientRenderManager;
import net.mcreator.borninchaosv.item.renderer.BonescallerStaffItemRenderer;
import net.mcreator.borninchaosv.item.renderer.PumpkinhandgunItemRenderer;
import net.mcreator.borninchaosv.item.renderer.PumpkinstaffaItemRenderer;
import net.mcreator.borninchaosv.item.renderer.StaffOfMagicArrowsAItemRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * 为生于混沌全部 4 个三维法杖/手枪渲染器替换冗余的 entityTranslucent 渲染类型，
 * 避免手持与物品栏渲染时无意义的半透明排序开销。
 */
@Mixin(value = {
        BonescallerStaffItemRenderer.class,
        PumpkinhandgunItemRenderer.class,
        PumpkinstaffaItemRenderer.class,
        StaffOfMagicArrowsAItemRenderer.class
})
public abstract class BicGeoItemRenderersMixin {
    @Redirect(
            method = "getRenderType*",
            remap = false,
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/RenderType;entityTranslucent(Lnet/minecraft/resources/ResourceLocation;)Lnet/minecraft/client/renderer/RenderType;",
                    remap = true
            )
    )
    private RenderType bic$resolveItemRenderType(ResourceLocation texture) {
        return BicClientRenderManager.resolveItemRenderType(texture);
    }
}
