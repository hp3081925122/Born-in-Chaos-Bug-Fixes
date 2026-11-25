package com.hp.borninchaosoptimizer.client;

import com.hp.borninchaosoptimizer.config.BicOptimizerConfig;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.ModList;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 世界实体渲染阶段的静态发光层面片合批缓存池：
 * 在 LevelRenderer 遍历实体期间收集各生物发光层的预变换顶点，在实体阶段末尾按 RenderType 一次性集中提交，
 * 避免每只实体在本体材质与发光眼材质之间反复切换触发 BufferSource.endBatch()。
 * 若检测到 Oculus / Iris 光影模组已加载，则自动停用延迟合批以保留逐实体光影 Uniform 绑定。
 */
@OnlyIn(Dist.CLIENT)
public final class BicGlowBatchBuffer {
    private static final int FLOATS_PER_VERTEX = 8;
    private static final boolean SHADER_MOD_LOADED =
            ModList.get().isLoaded("oculus") || ModList.get().isLoaded("iris");

    private static final Map<RenderType, RenderTypeBucket> BUCKETS = new LinkedHashMap<>();
    private static MultiBufferSource.BufferSource activeWorldBufferSource = null;

    private BicGlowBatchBuffer() {
    }

    public static void beginEntityPass(MultiBufferSource.BufferSource worldBufferSource) {
        reset();
        if (SHADER_MOD_LOADED || worldBufferSource == null) {
            return;
        }
        if (!BicOptimizerConfig.getBoolean(BicOptimizerConfig.CLIENT.enableClientOptimizations)
                || !BicOptimizerConfig.getBoolean(BicOptimizerConfig.CLIENT.batchEntityGlowLayers)) {
            return;
        }
        activeWorldBufferSource = worldBufferSource;
    }

    public static boolean canBatch(MultiBufferSource bufferSource, boolean animatedTexture) {
        return !animatedTexture && activeWorldBufferSource != null && bufferSource == activeWorldBufferSource;
    }

    public static RenderTypeBucket getOrCreateBucket(RenderType renderType) {
        return BUCKETS.computeIfAbsent(renderType, k -> new RenderTypeBucket());
    }

    public static void flushAndEndEntityPass(MultiBufferSource.BufferSource worldBufferSource) {
        if (activeWorldBufferSource == null) {
            return;
        }
        MultiBufferSource.BufferSource target = worldBufferSource != null ? worldBufferSource : activeWorldBufferSource;
        activeWorldBufferSource = null;
        for (Map.Entry<RenderType, RenderTypeBucket> entry : BUCKETS.entrySet()) {
            RenderTypeBucket bucket = entry.getValue();
            if (bucket.vertexCount == 0) {
                continue;
            }
            VertexConsumer consumer = target.getBuffer(entry.getKey());
            bucket.emitTo(consumer);
            bucket.clear();
        }
    }

    public static void reset() {
        activeWorldBufferSource = null;
        for (RenderTypeBucket bucket : BUCKETS.values()) {
            bucket.clear();
        }
    }

    public static final class RenderTypeBucket {
        private float[] floatData = new float[256 * FLOATS_PER_VERTEX];
        private int[] packedLights = new int[256];
        private int vertexCount = 0;

        public void pushVertex(float x, float y, float z, float u, float v, int packedLight, float nx, float ny, float nz) {
            if (this.vertexCount >= this.packedLights.length) {
                int newCap = this.packedLights.length << 1;
                this.floatData = Arrays.copyOf(this.floatData, newCap * FLOATS_PER_VERTEX);
                this.packedLights = Arrays.copyOf(this.packedLights, newCap);
            }
            int base = this.vertexCount * FLOATS_PER_VERTEX;
            this.floatData[base] = x;
            this.floatData[base + 1] = y;
            this.floatData[base + 2] = z;
            this.floatData[base + 3] = u;
            this.floatData[base + 4] = v;
            this.floatData[base + 5] = nx;
            this.floatData[base + 6] = ny;
            this.floatData[base + 7] = nz;
            this.packedLights[this.vertexCount] = packedLight;
            this.vertexCount++;
        }

        private void emitTo(VertexConsumer consumer) {
            float[] fd = this.floatData;
            int[] lights = this.packedLights;
            int count = this.vertexCount;
            for (int i = 0; i < count; i++) {
                int base = i * FLOATS_PER_VERTEX;
                consumer.vertex(
                        fd[base],
                        fd[base + 1],
                        fd[base + 2],
                        1.0f,
                        1.0f,
                        1.0f,
                        1.0f,
                        fd[base + 3],
                        fd[base + 4],
                        OverlayTexture.NO_OVERLAY,
                        lights[i],
                        fd[base + 5],
                        fd[base + 6],
                        fd[base + 7]
                );
            }
        }

        private void clear() {
            this.vertexCount = 0;
        }
    }
}
