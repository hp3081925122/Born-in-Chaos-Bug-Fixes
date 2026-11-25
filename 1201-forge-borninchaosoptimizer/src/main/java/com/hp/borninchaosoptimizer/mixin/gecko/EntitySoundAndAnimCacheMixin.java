package com.hp.borninchaosoptimizer.mixin.gecko;

import com.hp.borninchaosoptimizer.config.BicOptimizerConfig;
import com.hp.borninchaosoptimizer.util.BicResourceAndAnimCache;
import net.mcreator.borninchaosv.entity.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.IForgeRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import software.bernie.geckolib.core.animation.RawAnimation;

/**
 * 为全部 84 种生于混沌生物实体统一缓存：
 * 1. 环境音、脚步声、受击声与死亡声的 ResourceLocation 构造与注册表查表；
 * 2. GeckoLib 动画谓词每渲染帧重复创建的 RawAnimation 实例。
 */
@Mixin({
        BabySkeletonEntity.class,
        BabySkeletonMinionEntity.class,
        BabySpiderControlledEntity.class,
        BabySpiderEntity.class,
        BarrelZombieEntity.class,
        BloodyGadflyEntity.class,
        BoneImpEntity.class,
        BoneImpMinionEntity.class,
        BonescallerEntity.class,
        BonescallerNotDespawnEntity.class,
        ControlledBabySkeletonEntity.class,
        ControlledSpiritualAssistantEntity.class,
        CorpseFishEntity.class,
        CorpseFlyEntity.class,
        DarkVortexEntity.class,
        DecayingZombieEntity.class,
        DecayingZombieNotDespawnEntity.class,
        DecrepitSkeletonEntity.class,
        DiamondThermiteEntity.class,
        DireHoundLeaderEntity.class,
        DoorKnightEntity.class,
        DoorKnightNotDespawnEntity.class,
        DreadHoundEntity.class,
        DreadHoundNotDespawnEntity.class,
        FallenChaosKnightEntity.class,
        FelsteedEntity.class,
        FirelightEntity.class,
        FirelightNotDespawnEntity.class,
        GluttonFishEntity.class,
        InfernalSpiritEntity.class,
        KrampusEntity.class,
        KrampusHenchmanEntity.class,
        LifestealerEntity.class,
        LifestealerTrueFormEntity.class,
        LordPumpkinheadEntity.class,
        LordPumpkinheadHeadEntity.class,
        LordPumpkinheadWithoutaHorseEntity.class,
        LordTheHeadlessEntity.class,
        LordsFelsteedEntity.class,
        MaggotEntity.class,
        MissionaryRaiderEntity.class,
        MissionerEntity.class,
        MotherSpiderEntity.class,
        MrPumpkinControlledEntity.class,
        MrPumpkinEntity.class,
        MrsPumpkinEntity.class,
        NightmareStalkerEntity.class,
        PhantomBombEntityEntity.class,
        PhantomCreeperCopyEntity.class,
        PhantomCreeperEntity.class,
        PumpkinBombEntity.class,
        PumpkinBruiserEntity.class,
        PumpkinDunceEntity.class,
        PumpkinSpiritEntity.class,
        PumpkinheadEntity.class,
        RestlessSpiritEntity.class,
        RidingFelsteedEntity.class,
        RidingLordsFelsteedEntity.class,
        ScarletPersecutorEntity.class,
        SearedSpiritEntity.class,
        SearedSpiritNotDespawnEntity.class,
        SenorPumpkinEntity.class,
        SiameseSkeletonsEntity.class,
        SiameseSkeletonsleftEntity.class,
        SiameseSkeletonsrightEntity.class,
        SirPumpkinheadEntity.class,
        SirPumpkinheadWithoutHorseEntity.class,
        SirTheHeadlessEntity.class,
        SkeletonDemomanEntity.class,
        SkeletonThrasherEntity.class,
        SkeletonThrasherNotDespawnEntity.class,
        SpiritGuideAssistantEntity.class,
        SpiritGuideEntity.class,
        SpiritofChaosEntity.class,
        SupremeBonescallerEntity.class,
        SupremeBonescallerNotDespawnEntity.class,
        SupremeBonescallerStage2Entity.class,
        SwarmerEntity.class,
        ThornshellCrabEntity.class,
        ZombieBruiserEntity.class,
        ZombieClownEntity.class,
        ZombieClownNotDespawnEntity.class,
        ZombieFishermanEntity.class,
        ZombieLumberjackEntity.class
})
public abstract class EntitySoundAndAnimCacheMixin extends PathfinderMob {
    protected EntitySoundAndAnimCacheMixin(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
    }

    @Redirect(
            method = "*",
            require = 0,
            at = @At(
                    value = "NEW",
                    target = "(Ljava/lang/String;)Lnet/minecraft/resources/ResourceLocation;"
            )
    )
    private static ResourceLocation bic$cacheMobResourceNew(String location) {
        if (!BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.enableGlobalFixes)
                || !BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.cacheSoundsAndAnimations)) {
            return new ResourceLocation(location);
        }
        return BicResourceAndAnimCache.parseLocation(location);
    }

    @Redirect(
            method = {
                    "getAmbientSound()Lnet/minecraft/sounds/SoundEvent;",
                    "playStepSound(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)V",
                    "getHurtSound(Lnet/minecraft/world/damagesource/DamageSource;)Lnet/minecraft/sounds/SoundEvent;",
                    "getDeathSound()Lnet/minecraft/sounds/SoundEvent;"
            },
            require = 0,
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraftforge/registries/IForgeRegistry;getValue(Lnet/minecraft/resources/ResourceLocation;)Ljava/lang/Object;",
                    remap = false
            )
    )
    private <V> V bic$cacheEntityInstanceSoundLookup(IForgeRegistry<V> registry, ResourceLocation location) {
        if (!BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.enableGlobalFixes)
                || !BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.cacheSoundsAndAnimations)) {
            return registry.getValue(location);
        }
        return BicResourceAndAnimCache.lookupForgeRegistry(registry, location);
    }

    @Redirect(
            method = {"movementPredicate", "attackingPredicate", "procedurePredicate"},
            remap = false,
            require = 0,
            at = @At(
                    value = "INVOKE",
                    target = "Lsoftware/bernie/geckolib/core/animation/RawAnimation;begin()Lsoftware/bernie/geckolib/core/animation/RawAnimation;",
                    remap = false
            )
    )
    private RawAnimation bic$beginCachedAnim() {
        if (!BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.enableGlobalFixes)
                || !BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.cacheSoundsAndAnimations)) {
            return RawAnimation.begin();
        }
        return BicResourceAndAnimCache.DUMMY_BEGIN_SENTINEL;
    }

    @Redirect(
            method = {"movementPredicate", "attackingPredicate", "procedurePredicate"},
            remap = false,
            require = 0,
            at = @At(
                    value = "INVOKE",
                    target = "Lsoftware/bernie/geckolib/core/animation/RawAnimation;thenLoop(Ljava/lang/String;)Lsoftware/bernie/geckolib/core/animation/RawAnimation;",
                    remap = false
            )
    )
    private RawAnimation bic$thenLoopCachedAnim(RawAnimation instance, String animationName) {
        if (!BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.enableGlobalFixes)
                || !BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.cacheSoundsAndAnimations)) {
            return (instance == BicResourceAndAnimCache.DUMMY_BEGIN_SENTINEL ? RawAnimation.begin() : instance).thenLoop(animationName);
        }
        return BicResourceAndAnimCache.loopAnim(animationName);
    }

    @Redirect(
            method = {"movementPredicate", "attackingPredicate", "procedurePredicate"},
            remap = false,
            require = 0,
            at = @At(
                    value = "INVOKE",
                    target = "Lsoftware/bernie/geckolib/core/animation/RawAnimation;thenPlay(Ljava/lang/String;)Lsoftware/bernie/geckolib/core/animation/RawAnimation;",
                    remap = false
            )
    )
    private RawAnimation bic$thenPlayCachedAnim(RawAnimation instance, String animationName) {
        if (!BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.enableGlobalFixes)
                || !BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.cacheSoundsAndAnimations)) {
            return (instance == BicResourceAndAnimCache.DUMMY_BEGIN_SENTINEL ? RawAnimation.begin() : instance).thenPlay(animationName);
        }
        return BicResourceAndAnimCache.playAnim(animationName);
    }
}
