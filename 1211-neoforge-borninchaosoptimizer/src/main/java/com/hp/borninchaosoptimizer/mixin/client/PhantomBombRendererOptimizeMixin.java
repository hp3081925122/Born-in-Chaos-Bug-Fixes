package com.hp.borninchaosoptimizer.mixin.client;

import com.hp.borninchaosoptimizer.config.OptimizerConfig;
import com.hp.borninchaosoptimizer.util.BicResourceAndAnimCache;
import net.mcreator.borninchaosv.client.renderer.PhantomBombEntityRenderer;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 缓存幻影炸弹实体渲染器的基础贴图定位符，消除每帧重复调用 ResourceLocation.parse。
 */
@Mixin(value = PhantomBombEntityRenderer.class, remap = false)
public abstract class PhantomBombRendererOptimizeMixin {
    @Unique
    private static final ResourceLocation BIC$PHANTOM_BOMB_TEXTURE =
            BicResourceAndAnimCache.parseLocation("born_in_chaos_v1:textures/entities/phantombomb.png");

    @Inject(method = "getTextureLocation*", at = @At("HEAD"), cancellable = true)
    private void bic$cachePhantomBombTexture(CallbackInfoReturnable<ResourceLocation> cir) {
        if (OptimizerConfig.isEnabled(OptimizerConfig.CACHE_RESOURCE_LOCATIONS_AND_SOUNDS)) {
            cir.setReturnValue(BIC$PHANTOM_BOMB_TEXTURE);
        }
    }
}
