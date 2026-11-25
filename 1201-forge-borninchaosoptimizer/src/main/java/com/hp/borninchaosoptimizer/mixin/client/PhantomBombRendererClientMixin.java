package com.hp.borninchaosoptimizer.mixin.client;

import com.hp.borninchaosoptimizer.client.CachedPhantomBombGlowLayer;
import com.hp.borninchaosoptimizer.config.BicOptimizerConfig;
import net.mcreator.borninchaosv.client.model.Modelphantombomb;
import net.mcreator.borninchaosv.client.renderer.PhantomBombEntityRenderer;
import net.mcreator.borninchaosv.entity.PhantomBombEntityEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;

/**
 * 替换幻影炸弹渲染器的匿名发光层并缓存基础贴图 ResourceLocation，
 * 消除每渲染帧重复调用 bakeLayer 烘焙网格与新建 ResourceLocation 的开销。
 */
@Mixin(PhantomBombEntityRenderer.class)
public abstract class PhantomBombRendererClientMixin extends MobRenderer<PhantomBombEntityEntity, Modelphantombomb<PhantomBombEntityEntity>> {
    @Unique
    private static final ResourceLocation BIC_PHANTOM_BOMB_TEX =
            Objects.requireNonNull(ResourceLocation.tryBuild("born_in_chaos_v1", "textures/entities/phantombomb.png"));

    protected PhantomBombRendererClientMixin(EntityRendererProvider.Context context, Modelphantombomb<PhantomBombEntityEntity> model, float shadowRadius) {
        super(context, model, shadowRadius);
    }

    @Inject(method = "<init>", at = @At("RETURN"), remap = false)
    private void bic$replaceAnonymousGlowLayer(EntityRendererProvider.Context context, CallbackInfo ci) {
        this.layers.clear();
        this.addLayer(new CachedPhantomBombGlowLayer(this));
    }

    @Inject(method = "getTextureLocation(Lnet/mcreator/borninchaosv/entity/PhantomBombEntityEntity;)Lnet/minecraft/resources/ResourceLocation;", at = @At("HEAD"), cancellable = true, remap = false)
    private void bic$cachedPhantomBombTex(PhantomBombEntityEntity entity, CallbackInfoReturnable<ResourceLocation> cir) {
        if (BicOptimizerConfig.getBoolean(BicOptimizerConfig.CLIENT.enableClientOptimizations)
                && BicOptimizerConfig.getBoolean(BicOptimizerConfig.CLIENT.cacheGeoResourceLocations)) {
            cir.setReturnValue(BIC_PHANTOM_BOMB_TEX);
        }
    }
}
