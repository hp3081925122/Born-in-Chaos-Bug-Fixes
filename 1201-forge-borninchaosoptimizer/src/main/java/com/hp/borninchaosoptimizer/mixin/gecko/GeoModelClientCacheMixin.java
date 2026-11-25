package com.hp.borninchaosoptimizer.mixin.gecko;

import com.hp.borninchaosoptimizer.api.BicAnimatableEntity;
import com.hp.borninchaosoptimizer.config.BicOptimizerConfig;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoRenderer;

/**
 * 为生于混沌全部 83 个实体模型与 4 个物品模型缓存模型、动画与贴图 ResourceLocation，
 * 消除每帧每实体重复的字符串拼接、字符校验与对象分配。
 */
@Mixin(value = GeoModel.class, remap = false)
public abstract class GeoModelClientCacheMixin<T extends GeoAnimatable> {
    @Shadow(remap = false)
    public abstract ResourceLocation getModelResource(T animatable);

    @Shadow(remap = false)
    public abstract ResourceLocation getTextureResource(T animatable);

    @Shadow(remap = false)
    public abstract ResourceLocation getAnimationResource(T animatable);

    @Unique
    private boolean bic$isBicModel;

    @Unique
    private ResourceLocation bic$cachedModelLocation;

    @Unique
    private ResourceLocation bic$cachedStaticTextureLocation;

    @Unique
    private ResourceLocation bic$cachedAnimationLocation;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void bic$initBicFlag(CallbackInfo ci) {
        this.bic$isBicModel = this.getClass().getName().startsWith("net.mcreator.borninchaosv.");
    }

    @Inject(method = "getModelResource(Lsoftware/bernie/geckolib/core/animatable/GeoAnimatable;Lsoftware/bernie/geckolib/renderer/GeoRenderer;)Lnet/minecraft/resources/ResourceLocation;", at = @At("HEAD"), cancellable = true)
    private void bic$cacheModelResource(T animatable, GeoRenderer<T> renderer, CallbackInfoReturnable<ResourceLocation> cir) {
        if (!this.bic$isBicModel) {
            return;
        }
        if (!BicOptimizerConfig.getBoolean(BicOptimizerConfig.CLIENT.enableClientOptimizations)
                || !BicOptimizerConfig.getBoolean(BicOptimizerConfig.CLIENT.cacheGeoResourceLocations)) {
            return;
        }
        ResourceLocation cached = this.bic$cachedModelLocation;
        if (cached == null) {
            cached = this.getModelResource(animatable);
            this.bic$cachedModelLocation = cached;
        }
        cir.setReturnValue(cached);
    }

    @Inject(method = "getTextureResource(Lsoftware/bernie/geckolib/core/animatable/GeoAnimatable;Lsoftware/bernie/geckolib/renderer/GeoRenderer;)Lnet/minecraft/resources/ResourceLocation;", at = @At("HEAD"), cancellable = true)
    private void bic$cacheTextureResource(T animatable, GeoRenderer<T> renderer, CallbackInfoReturnable<ResourceLocation> cir) {
        if (!this.bic$isBicModel) {
            return;
        }
        if (!BicOptimizerConfig.getBoolean(BicOptimizerConfig.CLIENT.enableClientOptimizations)
                || !BicOptimizerConfig.getBoolean(BicOptimizerConfig.CLIENT.cacheGeoResourceLocations)) {
            return;
        }
        if (animatable instanceof BicAnimatableEntity bicEntity) {
            cir.setReturnValue(bicEntity.bic$getCachedTextureLocation());
            return;
        }
        ResourceLocation cached = this.bic$cachedStaticTextureLocation;
        if (cached == null) {
            cached = this.getTextureResource(animatable);
            this.bic$cachedStaticTextureLocation = cached;
        }
        cir.setReturnValue(cached);
    }

    @Redirect(
            method = "getAnimation",
            at = @At(
                    value = "INVOKE",
                    target = "Lsoftware/bernie/geckolib/model/GeoModel;getAnimationResource(Lsoftware/bernie/geckolib/core/animatable/GeoAnimatable;)Lnet/minecraft/resources/ResourceLocation;"
            )
    )
    private ResourceLocation bic$cacheAnimationResource(GeoModel<T> instance, T animatable) {
        if (!this.bic$isBicModel
                || !BicOptimizerConfig.getBoolean(BicOptimizerConfig.CLIENT.enableClientOptimizations)
                || !BicOptimizerConfig.getBoolean(BicOptimizerConfig.CLIENT.cacheGeoResourceLocations)) {
            return instance.getAnimationResource(animatable);
        }
        ResourceLocation cached = this.bic$cachedAnimationLocation;
        if (cached == null) {
            cached = instance.getAnimationResource(animatable);
            this.bic$cachedAnimationLocation = cached;
        }
        return cached;
    }
}
