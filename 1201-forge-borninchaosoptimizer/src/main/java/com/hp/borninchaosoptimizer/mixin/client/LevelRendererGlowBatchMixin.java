package com.hp.borninchaosoptimizer.mixin.client;

import com.hp.borninchaosoptimizer.client.BicGlowBatchBuffer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderBuffers;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 在 LevelRenderer.renderLevel 的世界实体遍历前后开启与提交静态发光层面片合批缓存，
 * 使同类生物本体与发光眼层各自合并为一次缓冲区提交。
 */
@Mixin(LevelRenderer.class)
public abstract class LevelRendererGlowBatchMixin {
    @Shadow
    @Final
    private RenderBuffers renderBuffers;

    @Inject(
            method = "renderLevel",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/multiplayer/ClientLevel;entitiesForRendering()Ljava/lang/Iterable;"
            )
    )
    private void bic$beginEntityGlowBatch(
            PoseStack poseStack,
            float partialTick,
            long finishNanoTime,
            boolean renderBlockOutline,
            Camera camera,
            GameRenderer gameRenderer,
            LightTexture lightTexture,
            Matrix4f projectionMatrix,
            CallbackInfo ci
    ) {
        BicGlowBatchBuffer.beginEntityPass(this.renderBuffers.bufferSource());
    }

    @Inject(
            method = "renderLevel",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;endLastBatch()V",
                    ordinal = 0
            )
    )
    private void bic$flushEntityGlowBatch(
            PoseStack poseStack,
            float partialTick,
            long finishNanoTime,
            boolean renderBlockOutline,
            Camera camera,
            GameRenderer gameRenderer,
            LightTexture lightTexture,
            Matrix4f projectionMatrix,
            CallbackInfo ci
    ) {
        BicGlowBatchBuffer.flushAndEndEntityPass(this.renderBuffers.bufferSource());
    }

    @Inject(method = "renderLevel", at = @At("RETURN"))
    private void bic$ensureGlowBatchReset(
            PoseStack poseStack,
            float partialTick,
            long finishNanoTime,
            boolean renderBlockOutline,
            Camera camera,
            GameRenderer gameRenderer,
            LightTexture lightTexture,
            Matrix4f projectionMatrix,
            CallbackInfo ci
    ) {
        BicGlowBatchBuffer.reset();
    }
}
