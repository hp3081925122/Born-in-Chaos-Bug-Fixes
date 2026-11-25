package com.hp.borninchaosoptimizer.client;

import com.hp.borninchaosoptimizer.Borninchaosoptimizer;
import com.hp.borninchaosoptimizer.config.OptimizerConfig;
import com.mojang.blaze3d.platform.NativeImage;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import java.io.InputStream;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.world.entity.Entity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.RegisterRenderBuffersEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

/**
 * 客户端专有渲染对象缓存与发光层固定缓冲合批调度器：
 * 1. 自动检测贴图是否含半透明像素（0 < alpha < 255），对纯不透明/二值镂空贴图使用 entityCutoutNoCull，消除每帧 CPU 面片深度排序与肢体穿模；
 * 2. 通过 RegisterRenderBuffersEvent 注册全部 31 种发光眼睛 RenderType 的独立缓冲，消除本体与发光层交替冲刷共享缓冲区的开销；
 * 3. 严格区分 MOD 总线与 GAME 总线事件订阅，并在安装 Iris / Oculus 光影模组时自动退回即时批次提交以保留逐实体光影 Uniform。
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

    private static final boolean SHADER_MOD_LOADED =
            ModList.get().isLoaded("iris") || ModList.get().isLoaded("oculus");

    private static final AtomicInteger RELOAD_EPOCH = new AtomicInteger(1);
    private static final Map<String, ResourceLocation> ENTITY_TEXTURE_CACHE = new ConcurrentHashMap<>(128);
    private static final Map<ResourceLocation, Boolean> SEMI_ALPHA_CACHE = new ConcurrentHashMap<>(128);
    private static final Map<ResourceLocation, RenderType> CUTOUT_RENDER_TYPE_CACHE = new ConcurrentHashMap<>(128);
    private static final Map<ResourceLocation, RenderType> TRANSLUCENT_RENDER_TYPE_CACHE = new ConcurrentHashMap<>(128);
    private static final Map<ResourceLocation, RenderType> EYES_RENDER_TYPE_CACHE = new ConcurrentHashMap<>(64);
    private static final Set<RenderType> ACTIVE_GLOW_BATCHES = new ReferenceOpenHashSet<>(32);
    private static RenderType[] ACTIVE_GLOW_SNAPSHOT = new RenderType[32];

    private ClientRenderCache() {
    }

    public static boolean isShaderModLoaded() {
        return SHADER_MOD_LOADED;
    }

    public static int getReloadEpoch() {
        return RELOAD_EPOCH.get();
    }

    public static void bumpReloadEpoch() {
        RELOAD_EPOCH.incrementAndGet();
        SEMI_ALPHA_CACHE.clear();
    }

    /**
     * 根据实体 getTexture() 名称直接映射至 ResourceLocation，消除每帧字符串拼接与解析开销。
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
     * 为实体解析最优 RenderType：隐身或含半透明像素的贴图保持 entityTranslucent，其余纯二值透明贴图使用 entityCutoutNoCull。
     */
    public static RenderType resolveEntityRenderType(Entity entity, ResourceLocation texture) {
        if (texture == null) {
            return null;
        }
        if (!OptimizerConfig.isEnabled(OptimizerConfig.OPTIMIZE_OPAQUE_ENTITY_RENDER_TYPES)
                || (entity != null && entity.isInvisible())
                || hasSemiTransparentPixels(texture)) {
            return TRANSLUCENT_RENDER_TYPE_CACHE.computeIfAbsent(texture, RenderType::entityTranslucent);
        }
        return CUTOUT_RENDER_TYPE_CACHE.computeIfAbsent(texture, RenderType::entityCutoutNoCull);
    }

    /**
     * 为三维武器/法杖解析最优 RenderType。
     */
    public static RenderType resolveItemRenderType(ResourceLocation texture) {
        if (texture == null) {
            return null;
        }
        if (!OptimizerConfig.isEnabled(OptimizerConfig.OPTIMIZE_OPAQUE_ENTITY_RENDER_TYPES)
                || hasSemiTransparentPixels(texture)) {
            return TRANSLUCENT_RENDER_TYPE_CACHE.computeIfAbsent(texture, RenderType::entityTranslucent);
        }
        return CUTOUT_RENDER_TYPE_CACHE.computeIfAbsent(texture, RenderType::entityCutoutNoCull);
    }

    /**
     * 缓存 RenderType.entityTranslucent(texture)。
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
        if (!SHADER_MOD_LOADED && glowType != null && Minecraft.getInstance().isSameThread()) {
            ACTIVE_GLOW_BATCHES.add(glowType);
        }
    }

    private static boolean hasSemiTransparentPixels(ResourceLocation texture) {
        return SEMI_ALPHA_CACHE.computeIfAbsent(texture, ClientRenderCache::inspectSemiTransparency);
    }

    private static boolean inspectSemiTransparency(ResourceLocation texture) {
        try {
            ResourceManager manager = Minecraft.getInstance().getResourceManager();
            Optional<Resource> optional = manager.getResource(texture);
            if (optional.isEmpty()) {
                return true;
            }
            try (InputStream stream = optional.get().open();
                 NativeImage image = NativeImage.read(NativeImage.Format.RGBA, stream)) {
                int w = image.getWidth();
                int h = image.getHeight();
                for (int y = 0; y < h; y++) {
                    for (int x = 0; x < w; x++) {
                        int alpha = (image.getPixelRGBA(x, y) >>> 24) & 0xFF;
                        if (alpha > 0 && alpha < 255) {
                            return true;
                        }
                    }
                }
                return false;
            }
        } catch (Exception e) {
            return true;
        }
    }

    @EventBusSubscriber(modid = Borninchaosoptimizer.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static final class ModBusEventsHandler {
        @SubscribeEvent
        public static void onRegisterRenderBuffers(RegisterRenderBuffersEvent event) {
            if (SHADER_MOD_LOADED) {
                return;
            }
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
        public static void onRegisterReloadListeners(RegisterClientReloadListenersEvent event) {
            event.registerReloadListener((ResourceManagerReloadListener) rm -> bumpReloadEpoch());
        }

        private ModBusEventsHandler() {
        }
    }

    @EventBusSubscriber(modid = Borninchaosoptimizer.MOD_ID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
    public static final class GameBusEventsHandler {
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

        private GameBusEventsHandler() {
        }
    }
}
