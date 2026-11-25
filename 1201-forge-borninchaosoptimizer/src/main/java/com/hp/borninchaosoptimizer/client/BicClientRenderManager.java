package com.hp.borninchaosoptimizer.client;

import com.hp.borninchaosoptimizer.Borninchaosoptimizer;
import com.hp.borninchaosoptimizer.config.BicOptimizerConfig;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.metadata.animation.AnimationMetadataSection;
import net.minecraft.client.resources.metadata.animation.FrameSize;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.cache.object.GeoCube;
import software.bernie.geckolib.cache.object.GeoQuad;
import software.bernie.geckolib.cache.object.GeoVertex;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 生于混沌客户端渲染缓存管理器：
 * 1. 动态检测贴图是否包含半透明像素（0 < alpha < 255），自动区分 entityCutoutNoCull 与 entityTranslucent；
 * 2. 解析发光贴图（*_e.png，含动态序列帧 .mcmeta）的非零像素分布，针对每个 BakedGeoModel 预编译仅含有效发光骨骼与面片的稀疏渲染计划；
 * 3. 在客户端重载资源包（F3+T）时自动清空全部缓存以兼容材质包切换。
 */
@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid = Borninchaosoptimizer.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class BicClientRenderManager {
    private static final Map<ResourceLocation, Boolean> SEMI_ALPHA_CACHE = new ConcurrentHashMap<>();
    private static final Map<ResourceLocation, RenderType> CUTOUT_TYPE_CACHE = new ConcurrentHashMap<>();
    private static final Map<ResourceLocation, RenderType> TRANSLUCENT_TYPE_CACHE = new ConcurrentHashMap<>();
    private static final Map<ResourceLocation, RenderType> EYES_TYPE_CACHE = new ConcurrentHashMap<>();
    private static final Map<BakedGeoModel, Map<ResourceLocation, GlowModelPlan>> GLOW_PLAN_CACHE =
            Collections.synchronizedMap(new WeakHashMap<>());

    private BicClientRenderManager() {
    }

    @SubscribeEvent
    public static void onRegisterReloadListeners(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener((ResourceManagerReloadListener) BicClientRenderManager::clearCaches);
    }

    public static void clearCaches(ResourceManager resourceManager) {
        SEMI_ALPHA_CACHE.clear();
        CUTOUT_TYPE_CACHE.clear();
        TRANSLUCENT_TYPE_CACHE.clear();
        EYES_TYPE_CACHE.clear();
        GLOW_PLAN_CACHE.clear();
    }

    public static RenderType getCachedEyesRenderType(ResourceLocation glowTexture) {
        return EYES_TYPE_CACHE.computeIfAbsent(glowTexture, RenderType::eyes);
    }

    public static RenderType resolveEntityRenderType(Entity entity, ResourceLocation texture) {
        if (!BicOptimizerConfig.getBoolean(BicOptimizerConfig.CLIENT.enableClientOptimizations)
                || !BicOptimizerConfig.getBoolean(BicOptimizerConfig.CLIENT.optimizeOpaqueEntityRenderTypes)) {
            return TRANSLUCENT_TYPE_CACHE.computeIfAbsent(texture, RenderType::entityTranslucent);
        }
        if (entity.isInvisible() || hasSemiTransparentPixels(texture)) {
            return TRANSLUCENT_TYPE_CACHE.computeIfAbsent(texture, RenderType::entityTranslucent);
        }
        return CUTOUT_TYPE_CACHE.computeIfAbsent(texture, RenderType::entityCutoutNoCull);
    }

    public static RenderType resolveItemRenderType(ResourceLocation texture) {
        if (!BicOptimizerConfig.getBoolean(BicOptimizerConfig.CLIENT.enableClientOptimizations)
                || !BicOptimizerConfig.getBoolean(BicOptimizerConfig.CLIENT.optimizeOpaqueEntityRenderTypes)) {
            return TRANSLUCENT_TYPE_CACHE.computeIfAbsent(texture, RenderType::entityTranslucent);
        }
        if (hasSemiTransparentPixels(texture)) {
            return TRANSLUCENT_TYPE_CACHE.computeIfAbsent(texture, RenderType::entityTranslucent);
        }
        return CUTOUT_TYPE_CACHE.computeIfAbsent(texture, RenderType::entityCutoutNoCull);
    }

    private static boolean hasSemiTransparentPixels(ResourceLocation texture) {
        return SEMI_ALPHA_CACHE.computeIfAbsent(texture, BicClientRenderManager::inspectSemiTransparency);
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

    public static GlowModelPlan getGlowPlan(BakedGeoModel bakedModel, ResourceLocation glowTexture) {
        Map<ResourceLocation, GlowModelPlan> perTexture;
        synchronized (GLOW_PLAN_CACHE) {
            perTexture = GLOW_PLAN_CACHE.computeIfAbsent(bakedModel, k -> new HashMap<>(2));
            GlowModelPlan existing = perTexture.get(glowTexture);
            if (existing != null) {
                return existing;
            }
        }
        GlowModelPlan compiled = compileGlowPlan(bakedModel, glowTexture);
        synchronized (GLOW_PLAN_CACHE) {
            Map<ResourceLocation, GlowModelPlan> map = GLOW_PLAN_CACHE.computeIfAbsent(bakedModel, k -> new HashMap<>(2));
            map.put(glowTexture, compiled);
        }
        return compiled;
    }

    private static GlowModelPlan compileGlowPlan(BakedGeoModel bakedModel, ResourceLocation glowTexture) {
        GlowMask mask = loadGlowMask(glowTexture);
        List<GlowBoneNode> rootNodes = new ArrayList<>();
        for (GeoBone rootBone : bakedModel.topLevelBones()) {
            GlowBoneNode pruned = buildBoneNode(rootBone, mask);
            if (pruned != null) {
                rootNodes.add(pruned);
            }
        }
        return new GlowModelPlan(rootNodes.toArray(GlowBoneNode[]::new), mask != null && mask.animated);
    }

    private static GlowBoneNode buildBoneNode(GeoBone bone, GlowMask mask) {
        if (Boolean.TRUE.equals(bone.shouldNeverRender())) {
            return null;
        }
        List<GlowCubeNode> activeCubes = new ArrayList<>();
        for (GeoCube cube : bone.getCubes()) {
            List<GeoQuad> activeQuads = new ArrayList<>(6);
            for (GeoQuad quad : cube.quads()) {
                if (quad != null && (mask == null || mask.intersectsQuad(quad))) {
                    activeQuads.add(quad);
                }
            }
            if (!activeQuads.isEmpty()) {
                activeCubes.add(new GlowCubeNode(cube, activeQuads.toArray(GeoQuad[]::new)));
            }
        }

        List<GlowBoneNode> activeChildren = new ArrayList<>();
        for (GeoBone child : bone.getChildBones()) {
            GlowBoneNode childNode = buildBoneNode(child, mask);
            if (childNode != null) {
                activeChildren.add(childNode);
            }
        }

        if (activeCubes.isEmpty() && activeChildren.isEmpty()) {
            return null;
        }
        return new GlowBoneNode(
                bone,
                activeCubes.toArray(GlowCubeNode[]::new),
                activeChildren.toArray(GlowBoneNode[]::new)
        );
    }

    private static GlowMask loadGlowMask(ResourceLocation glowTexture) {
        try {
            ResourceManager manager = Minecraft.getInstance().getResourceManager();
            Optional<Resource> optional = manager.getResource(glowTexture);
            if (optional.isEmpty()) {
                return null;
            }
            Resource resource = optional.get();
            try (InputStream stream = resource.open();
                 NativeImage image = NativeImage.read(NativeImage.Format.RGBA, stream)) {
                int imgW = image.getWidth();
                int imgH = image.getHeight();
                if (imgW <= 0 || imgH <= 0) {
                    return null;
                }
                Optional<AnimationMetadataSection> animMetaOpt = resource.metadata().getSection(AnimationMetadataSection.SERIALIZER);
                int frameW = imgW;
                int frameH = imgH;
                boolean animated = false;
                if (animMetaOpt.isPresent()) {
                    FrameSize frameSize = animMetaOpt.get().calculateFrameSize(imgW, imgH);
                    if (frameSize.width() > 0 && frameSize.height() > 0
                            && imgW % frameSize.width() == 0 && imgH % frameSize.height() == 0) {
                        frameW = frameSize.width();
                        frameH = frameSize.height();
                        animated = (imgW > frameW || imgH > frameH);
                    }
                }
                int cols = Math.max(1, imgW / frameW);
                int rows = Math.max(1, imgH / frameH);
                boolean[] active = new boolean[frameW * frameH];
                for (int row = 0; row < rows; row++) {
                    int baseY = row * frameH;
                    for (int col = 0; col < cols; col++) {
                        int baseX = col * frameW;
                        for (int y = 0; y < frameH; y++) {
                            int rowOffset = y * frameW;
                            for (int x = 0; x < frameW; x++) {
                                if (active[rowOffset + x]) {
                                    continue;
                                }
                                int abgr = image.getPixelRGBA(baseX + x, baseY + y);
                                int alpha = (abgr >>> 24) & 0xFF;
                                int bgr = abgr & 0x00FFFFFF;
                                if (alpha > 0 && bgr != 0) {
                                    active[rowOffset + x] = true;
                                }
                            }
                        }
                    }
                }
                return new GlowMask(frameW, frameH, active, animated);
            }
        } catch (Exception e) {
            return null;
        }
    }

    private static final class GlowMask {
        private final int width;
        private final int height;
        private final boolean[] activePixels;
        private final boolean animated;

        private GlowMask(int width, int height, boolean[] activePixels, boolean animated) {
            this.width = width;
            this.height = height;
            this.activePixels = activePixels;
            this.animated = animated;
        }

        private boolean intersectsQuad(GeoQuad quad) {
            GeoVertex[] vertices = quad.vertices();
            if (vertices == null || vertices.length == 0) {
                return false;
            }
            float minU = Float.POSITIVE_INFINITY;
            float maxU = Float.NEGATIVE_INFINITY;
            float minV = Float.POSITIVE_INFINITY;
            float maxV = Float.NEGATIVE_INFINITY;
            for (GeoVertex v : vertices) {
                float u = v.texU();
                float tv = v.texV();
                if (u < minU) minU = u;
                if (u > maxU) maxU = u;
                if (tv < minV) minV = tv;
                if (tv > maxV) maxV = tv;
            }
            int x0 = Math.max(0, (int) Math.floor(minU * this.width) - 1);
            int x1 = Math.min(this.width - 1, (int) Math.ceil(maxU * this.width) + 1);
            int y0 = Math.max(0, (int) Math.floor(minV * this.height) - 1);
            int y1 = Math.min(this.height - 1, (int) Math.ceil(maxV * this.height) + 1);
            if (x0 > x1 || y0 > y1) {
                return false;
            }
            for (int y = y0; y <= y1; y++) {
                int rowOffset = y * this.width;
                for (int x = x0; x <= x1; x++) {
                    if (this.activePixels[rowOffset + x]) {
                        return true;
                    }
                }
            }
            return false;
        }
    }

    public record GlowModelPlan(GlowBoneNode[] rootBones, boolean isAnimated) {
        public boolean isEmpty() {
            return this.rootBones.length == 0;
        }
    }

    public record GlowBoneNode(GeoBone liveBone, GlowCubeNode[] cubes, GlowBoneNode[] children) {
    }

    public record GlowCubeNode(GeoCube cube, GeoQuad[] quads) {
    }
}
