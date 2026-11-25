package com.hp.borninchaosoptimizer.mixin.gecko;

import com.hp.borninchaosoptimizer.config.BicOptimizerConfig;
import com.hp.borninchaosoptimizer.util.BicResourceAndAnimCache;
import net.mcreator.borninchaosv.entity.*;
import net.mcreator.borninchaosv.item.BonescallerStaffItem;
import net.mcreator.borninchaosv.item.PumpkinhandgunItem;
import net.mcreator.borninchaosv.item.PumpkinstaffaItem;
import net.mcreator.borninchaosv.item.StaffOfMagicArrowsAItem;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import software.bernie.geckolib.core.animation.RawAnimation;

/**
 * 为全部 14 种生于混沌弹射物实体及 4 种 GeckoLib 三维动画物品统一缓存：
 * 1. 弹射物发射静态方法中的音效 ResourceLocation 构造与注册表查表；
 * 2. GeckoLib 动画物品谓词每渲染帧重复创建的 RawAnimation 实例。
 */
@Mixin(value = {
        IntoxicatindBombProjectileEntity.class,
        MissionaryChargeEntity.class,
        MonstrousEasterEggProjectileEntity.class,
        PhantomBombProjectileEntity.class,
        PumpkinPistol2ProjectileEntity.class,
        PumpkinPistolProjectileEntity.class,
        PumpkinStaff2ProjectileEntity.class,
        PumpkinStaffProjectileEntity.class,
        RottenEasterEggProjectileEntity.class,
        SpiritualEasterEggProjectileEntity.class,
        StaffofBlindnessProjectileEntity.class,
        StaffofMagicArrows2ProjectileEntity.class,
        StaffofMagicArrowsProjectileEntity.class,
        StimulatingBombprojectileEntity.class,
        BonescallerStaffItem.class,
        PumpkinhandgunItem.class,
        PumpkinstaffaItem.class,
        StaffOfMagicArrowsAItem.class
}, remap = false)
public abstract class ProjectileAndItemSoundAnimCacheMixin {
    @Redirect(
            method = "shoot",
            require = 0,
            at = @At(
                    value = "NEW",
                    target = "(Ljava/lang/String;)Lnet/minecraft/resources/ResourceLocation;"
            )
    )
    private static ResourceLocation bic$cacheProjectileResourceNew(String location) {
        if (!BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.enableGlobalFixes)
                || !BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.cacheSoundsAndAnimations)) {
            return new ResourceLocation(location);
        }
        return BicResourceAndAnimCache.parseLocation(location);
    }

    @Redirect(
            method = "shoot",
            require = 0,
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraftforge/registries/IForgeRegistry;getValue(Lnet/minecraft/resources/ResourceLocation;)Ljava/lang/Object;",
                    remap = false
            )
    )
    private static <V> V bic$cacheProjectileStaticSoundLookup(IForgeRegistry<V> registry, ResourceLocation location) {
        if (!BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.enableGlobalFixes)
                || !BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.cacheSoundsAndAnimations)) {
            return registry.getValue(location);
        }
        return BicResourceAndAnimCache.lookupForgeRegistry(registry, location);
    }

    @Redirect(
            method = {"idlePredicate", "procedurePredicate"},
            require = 0,
            at = @At(
                    value = "INVOKE",
                    target = "Lsoftware/bernie/geckolib/core/animation/RawAnimation;begin()Lsoftware/bernie/geckolib/core/animation/RawAnimation;",
                    remap = false
            )
    )
    private RawAnimation bic$beginCachedItemAnim() {
        if (!BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.enableGlobalFixes)
                || !BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.cacheSoundsAndAnimations)) {
            return RawAnimation.begin();
        }
        return BicResourceAndAnimCache.DUMMY_BEGIN_SENTINEL;
    }

    @Redirect(
            method = {"idlePredicate", "procedurePredicate"},
            require = 0,
            at = @At(
                    value = "INVOKE",
                    target = "Lsoftware/bernie/geckolib/core/animation/RawAnimation;thenLoop(Ljava/lang/String;)Lsoftware/bernie/geckolib/core/animation/RawAnimation;",
                    remap = false
            )
    )
    private RawAnimation bic$thenLoopCachedItemAnim(RawAnimation instance, String animationName) {
        if (!BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.enableGlobalFixes)
                || !BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.cacheSoundsAndAnimations)) {
            return (instance == BicResourceAndAnimCache.DUMMY_BEGIN_SENTINEL ? RawAnimation.begin() : instance).thenLoop(animationName);
        }
        return BicResourceAndAnimCache.loopAnim(animationName);
    }

    @Redirect(
            method = {"idlePredicate", "procedurePredicate"},
            require = 0,
            at = @At(
                    value = "INVOKE",
                    target = "Lsoftware/bernie/geckolib/core/animation/RawAnimation;thenPlay(Ljava/lang/String;)Lsoftware/bernie/geckolib/core/animation/RawAnimation;",
                    remap = false
            )
    )
    private RawAnimation bic$thenPlayCachedItemAnim(RawAnimation instance, String animationName) {
        if (!BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.enableGlobalFixes)
                || !BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.cacheSoundsAndAnimations)) {
            return (instance == BicResourceAndAnimCache.DUMMY_BEGIN_SENTINEL ? RawAnimation.begin() : instance).thenPlay(animationName);
        }
        return BicResourceAndAnimCache.playAnim(animationName);
    }
}
