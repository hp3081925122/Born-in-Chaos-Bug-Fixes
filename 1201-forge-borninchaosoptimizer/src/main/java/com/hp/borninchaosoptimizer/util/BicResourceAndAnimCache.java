package com.hp.borninchaosoptimizer.util;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import software.bernie.geckolib.core.animation.RawAnimation;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 双端通用的资源定位符、音效注册表与 GeckoLib 预构建动画缓存池。
 * 不引用任何客户端专有类，可在专用服务端与客户端安全共享。
 */
public final class BicResourceAndAnimCache {
    /**
     * 仅作为 RawAnimation.begin() 重定向哨兵占位符使用，紧随其后的 thenLoop/thenPlay 将直接返回缓存实例。
     */
    public static final RawAnimation DUMMY_BEGIN_SENTINEL = RawAnimation.begin();

    private static final Map<String, ResourceLocation> LOCATION_CACHE = new ConcurrentHashMap<>(256);
    private static final Map<ResourceLocation, SoundEvent> SOUND_CACHE = new ConcurrentHashMap<>(128);
    private static final Map<String, RawAnimation> LOOP_ANIM_CACHE = new ConcurrentHashMap<>(32);
    private static final Map<String, RawAnimation> PLAY_ANIM_CACHE = new ConcurrentHashMap<>(64);

    private BicResourceAndAnimCache() {
    }

    /**
     * 解析并缓存 ResourceLocation，消除每帧/每步重复创建与校验字符串的开销。
     */
    public static ResourceLocation parseLocation(String location) {
        if (location == null) {
            return null;
        }
        return LOCATION_CACHE.computeIfAbsent(location, ResourceLocation::new);
    }

    /**
     * 缓存 Forge 音效注册表查表结果，避免每次播放环境音/脚步声/受击声时重复哈希查找。
     */
    public static <V> V lookupForgeRegistry(IForgeRegistry<V> registry, ResourceLocation key) {
        if (key == null) {
            return registry.getValue(null);
        }
        if (registry == ForgeRegistries.SOUND_EVENTS) {
            SoundEvent cached = SOUND_CACHE.get(key);
            if (cached != null) {
                @SuppressWarnings("unchecked")
                V cast = (V) cached;
                return cast;
            }
            SoundEvent resolved = ForgeRegistries.SOUND_EVENTS.getValue(key);
            if (resolved != null) {
                SOUND_CACHE.put(key, resolved);
            }
            @SuppressWarnings("unchecked")
            V cast = (V) resolved;
            return cast;
        }
        return registry.getValue(key);
    }

    /**
     * 获取或创建单阶段循环动画实例，供 GeckoLib 动画控制器直接命中引用相等快速路径。
     */
    public static RawAnimation loopAnim(String animationName) {
        if (animationName == null) {
            return RawAnimation.begin().thenLoop(null);
        }
        return LOOP_ANIM_CACHE.computeIfAbsent(animationName, name -> RawAnimation.begin().thenLoop(name));
    }

    /**
     * 获取或创建单阶段单次播放动画实例。
     */
    public static RawAnimation playAnim(String animationName) {
        if (animationName == null) {
            return RawAnimation.begin().thenPlay(null);
        }
        return PLAY_ANIM_CACHE.computeIfAbsent(animationName, name -> RawAnimation.begin().thenPlay(name));
    }
}
