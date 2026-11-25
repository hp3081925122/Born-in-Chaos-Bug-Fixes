package com.hp.borninchaosoptimizer.util;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import software.bernie.geckolib.animation.RawAnimation;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 双端通用的资源定位符、注册表音效与 GeckoLib 预构建动画缓存池。
 * 严格不引用任何客户端专有类，可在专用服务端与客户端安全共享。
 */
public final class BicResourceAndAnimCache {
    /**
     * 仅作为 RawAnimation.begin() 重定向占位符使用，后续紧跟的 thenLoop/thenPlay 会返回不可变缓存实例而不会修改此对象。
     */
    public static final RawAnimation DUMMY_BEGIN_SENTINEL = RawAnimation.begin();

    private static final Map<String, ResourceLocation> LOCATION_CACHE = new ConcurrentHashMap<>(256);
    private static final Map<ResourceLocation, SoundEvent> SOUND_CACHE = new ConcurrentHashMap<>(128);
    private static final Map<String, RawAnimation> LOOP_ANIM_CACHE = new ConcurrentHashMap<>(32);
    private static final Map<String, RawAnimation> PLAY_ANIM_CACHE = new ConcurrentHashMap<>(64);

    /**
     * 解析并缓存 ResourceLocation，消除每帧/每步重复解析字符串的开销。
     */
    public static ResourceLocation parseLocation(String location) {
        if (location == null) {
            return null;
        }
        return LOCATION_CACHE.computeIfAbsent(location, ResourceLocation::parse);
    }

    /**
     * 针对 SOUND_EVENT 注册表的快速缓存查找，避免每次播放环境音/脚步声/受击声时重复哈希查表。
     */
    public static <T> T lookupRegistry(Registry<T> registry, ResourceLocation location) {
        if (location == null) {
            return registry.get((ResourceLocation) null);
        }
        if (registry == BuiltInRegistries.SOUND_EVENT) {
            SoundEvent cached = SOUND_CACHE.get(location);
            if (cached != null) {
                @SuppressWarnings("unchecked")
                T cast = (T) cached;
                return cast;
            }
            SoundEvent resolved = BuiltInRegistries.SOUND_EVENT.get(location);
            if (resolved != null) {
                SOUND_CACHE.put(location, resolved);
            }
            @SuppressWarnings("unchecked")
            T cast = (T) resolved;
            return cast;
        }
        return registry.get(location);
    }

    /**
     * 获取或创建单阶段循环动画实例，供 GeckoLib 动画控制器复用同一引用以加速相等性判断。
     */
    public static RawAnimation loopAnim(String animationName) {
        if (animationName == null) {
            return RawAnimation.begin().thenLoop((String) null);
        }
        return LOOP_ANIM_CACHE.computeIfAbsent(animationName, name -> RawAnimation.begin().thenLoop(name));
    }

    /**
     * 获取或创建单阶段单次播放动画实例。
     */
    public static RawAnimation playAnim(String animationName) {
        if (animationName == null) {
            return RawAnimation.begin().thenPlay((String) null);
        }
        return PLAY_ANIM_CACHE.computeIfAbsent(animationName, name -> RawAnimation.begin().thenPlay(name));
    }

    private BicResourceAndAnimCache() {
    }
}
