package com.hp.borninchaosoptimizer.mixin.client;

import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.resources.metadata.animation.AnimationMetadataSection;
import net.minecraft.client.resources.metadata.animation.FrameSize;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 修复生于混沌单帧粒子贴图（如 16x16 的 born_in_chaos_v1:dimanim）附带越界帧索引 [0..6] 的 .mcmeta，
 * 导致客户端每次加载贴图集刷屏 "Invalid frame index on sprite born_in_chaos_v1:dimanim" 的缺陷。
 */
@Mixin(SpriteContents.class)
public abstract class BicSpriteFrameFixMixin {
    @Shadow
    @Final
    private ResourceLocation name;

    @Inject(method = "createAnimatedTexture", at = @At("HEAD"), cancellable = true)
    private void bic$skipInvalidSingleFrameAnimation(
            FrameSize frameSize, int width, int height, AnimationMetadataSection metadata,
            CallbackInfoReturnable<Object> cir
    ) {
        if (this.name == null || !"born_in_chaos_v1".equals(this.name.getNamespace())) {
            return;
        }
        if (frameSize == null || frameSize.width() <= 0 || frameSize.height() <= 0) {
            return;
        }
        int frameCount = (width / frameSize.width()) * (height / frameSize.height());
        if (frameCount <= 1) {
            cir.setReturnValue(null);
        }
    }
}
