package com.hp.borninchaosoptimizer.client;

import com.hp.borninchaosoptimizer.Borninchaosoptimizer;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterRenderBuffersEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 客户端专有渲染对象缓存与发光层固定缓冲合批调度器。
 * 通过向 NeoForge 的 RegisterRenderBuffersEvent 注册原模组全部 31 种发光眼睛 RenderType 的独立缓冲，
 * 避免每个实体在本体半透明层与发光层交替渲染时反复强制刷新 MultiBufferSource.lastSharedType、重复对顶点排序和切换着色器；
 * 并在 RenderLevelStageEvent.Stage.AFTER_ENTITIES 阶段（水体与半透明方块渲染前）集中提交当帧激活的发光缓冲，
 * 100% 保持原版发光叠加、水下透视和光影模组兼容性。
 */
public final class ClientRenderCache {
    private static final String[] GLOW_TEXTURE_PATHS = {
            "textures/entities/babyskeleton_e.png",
            "textures/entities/babyspider_e.png",
            "textures/entities/babyspider_friendly_e.png",
            "textures/entities/bloodygadfly_e.png",
            "textures/entities/bone_imp_e.png",
            "textures/entities/corpsefish_e.png",
            "textures/entities/corpsefly_e.png",
            "textures/entities/darkvortexn_e.png",
            "textures/entities/diamond_termite_e.png",
            "textures/entities/direhoundleader_e.png",
            "textures/entities/dreadhound_e.png",
            "textures/entities/fallenchaosknightn_e.png",
            "textures/entities/firelight_e.png",
            "textures/entities/gluttonfish_e.png",
            "textures/entities/infernal_spirit_e.png",
            "textures/entities/krampus_e.png",
            "textures/entities/krampushenchman_e.png",
            "textures/entities/lifestealer_trueform_e.png",
            "textures/entities/lordofpumpkins_e.png",
            "textures/entities/lordofpumpkinss_e.png",
            "textures/entities/mrpumpkin_aggressive_e.png",
            "textures/entities/nightmarestalker_e.png",
            "textures/entities/phantombomb_e.png",
            "textures/entities/phantomcreeper_e.png",
            "textures/entities/pumpkinbomb_e.png",
            "textures/entities/pumpkinbruiser_e.png",
            "textures/entities/pumpkindunce_e.png",
            "textures/entities/pumpkinheadrider_e.png",
            "textures/entities/seared_spirit_e.png",
            "textures/entities/senorpumpkin_e.png",
            "textures/entities/spidersmother_e.png"
    };

    private static final AtomicInteger RELOAD_EPOCH = new AtomicInteger(1);
    private static final Map<String, ResourceLocation> ENTITY_TEXTURE_CACHE = new ConcurrentHashMap<>(128);
    private static final Map<ResourceLocation, RenderType> TRANSLUCENT_RENDER_TYPE_CACHE = new ConcurrentHashMap<>(128);
    private static final Map<ResourceLocation, RenderType> EYES_RENDER_TYPE_CACHE = new ConcurrentHashMap<>(64);
    private static final Set<RenderType> ACTIVE_GLOW_BATCHES = new ReferenceOpenHashSet<>(32);
    private static RenderType[] ACTIVE_GLOW_SNAPSHOT = new RenderType[32];

    public static int getReloadEpoch() {
        return RELOAD_EPOCH.get();
    }

    public static void bumpReloadEpoch() {
        RELOAD_EPOCH.incrementAndGet();
    }

    /**
     * 根据实体 getTexture() 名称直接映射至 ResourceLocation，彻底消除每帧字符串拼接与解析开销。
     */
    public static ResourceLocation entityTexture(String textureName) {
        if (textureName == null || textureName.isEmpty()) {
            return ResourceLocation.fromNamespaceAndPath("born_in_chaos_v1", "textures/entities/.png");
        }
        return ENTITY_TEXTURE_CACHE.computeIfAbsent(
                textureName,
                key -> ResourceLocation.fromNamespaceAndPath("born_in_chaos_v1", "textures/entities/" + key + ".png")
        );
    }

    /**
     * 缓存 RenderType.entityTranslucent(texture)，消除 Util.memoize(BiFunction) 每次调用分配 Pair 包装对象的开销。
     */
    public static RenderType entityTranslucent(ResourceLocation texture) {
        if (texture == null) {
            return null;
        }
        return TRANSLUCENT_RENDER_TYPE_CACHE.computeIfAbsent(texture, RenderType::entityTranslucent);
    }

    /**
     * 缓存 RenderType.eyes(texture)。
     */
    public static RenderType eyes(ResourceLocation texture) {
        if (texture == null) {
            return null;
        }
        return EYES_RENDER_TYPE_CACHE.computeIfAbsent(texture, RenderType::eyes);
    }

    /**
     * 记录当前渲染帧中使用过的发光层 RenderType，以便在实体渲染阶段末尾一次性合批绘制。
     */
    public static void markGlowActive(RenderType glowType) {
        if (glowType != null && Minecraft.getInstance().isSameThread()) {
            ACTIVE_GLOW_BATCHES.add(glowType);
        }
    }

    private ClientRenderCache() {
    }

    @EventBusSubscriber(modid = Borninchaosoptimizer.MOD_ID, value = Dist.CLIENT)
    public static final class ClientEventsHandler {
        @SubscribeEvent
        public static void onRegisterRenderBuffers(RegisterRenderBuffersEvent event) {
            for (String path : GLOW_TEXTURE_PATHS) {
                ResourceLocation loc = ResourceLocation.fromNamespaceAndPath("born_in_chaos_v1", path);
                RenderType glowType = eyes(loc);
                event.registerRenderBuffer(glowType);
            }
        }

        @SubscribeEvent
        public static void onAddLayers(EntityRenderersEvent.AddLayers event) {
            bumpReloadEpoch();
        }

        @SubscribeEvent
        public static void onRenderLevelStage(RenderLevelStageEvent event) {
            if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_ENTITIES || ACTIVE_GLOW_BATCHES.isEmpty()) {
                return;
            }
            MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
            int count = ACTIVE_GLOW_BATCHES.size();
            if (ACTIVE_GLOW_SNAPSHOT.length < count) {
                ACTIVE_GLOW_SNAPSHOT = new RenderType[count + 8];
            }
            int idx = 0;
            for (RenderType type : ACTIVE_GLOW_BATCHES) {
                ACTIVE_GLOW_SNAPSHOT[idx++] = type;
            }
            ACTIVE_GLOW_BATCHES.clear();
            for (int i = 0; i < idx; i++) {
                RenderType type = ACTIVE_GLOW_SNAPSHOT[i];
                ACTIVE_GLOW_SNAPSHOT[i] = null;
                bufferSource.endBatch(type);
            }
        }

        private ClientEventsHandler() {
        }
    }
}
