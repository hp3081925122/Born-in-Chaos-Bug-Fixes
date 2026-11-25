package com.hp.borninchaosoptimizer.mixin;

import com.hp.borninchaosoptimizer.config.OptimizerConfig;
import com.hp.borninchaosoptimizer.util.BicResourceAndAnimCache;
import net.mcreator.borninchaosv.entity.BabySkeletonEntity;
import net.mcreator.borninchaosv.entity.BabySkeletonMinionEntity;
import net.mcreator.borninchaosv.entity.BabySpiderControlledEntity;
import net.mcreator.borninchaosv.entity.BabySpiderEntity;
import net.mcreator.borninchaosv.entity.BarrelZombieEntity;
import net.mcreator.borninchaosv.entity.BloodyGadflyEntity;
import net.mcreator.borninchaosv.entity.BoneImpEntity;
import net.mcreator.borninchaosv.entity.BoneImpMinionEntity;
import net.mcreator.borninchaosv.entity.BonescallerEntity;
import net.mcreator.borninchaosv.entity.BonescallerNotDespawnEntity;
import net.mcreator.borninchaosv.entity.ControlledBabySkeletonEntity;
import net.mcreator.borninchaosv.entity.ControlledSpiritualAssistantEntity;
import net.mcreator.borninchaosv.entity.CorpseFishEntity;
import net.mcreator.borninchaosv.entity.CorpseFlyEntity;
import net.mcreator.borninchaosv.entity.DarkVortexEntity;
import net.mcreator.borninchaosv.entity.DecayingZombieEntity;
import net.mcreator.borninchaosv.entity.DecayingZombieNotDespawnEntity;
import net.mcreator.borninchaosv.entity.DecrepitSkeletonEntity;
import net.mcreator.borninchaosv.entity.DiamondThermiteEntity;
import net.mcreator.borninchaosv.entity.DireHoundLeaderEntity;
import net.mcreator.borninchaosv.entity.DoorKnightEntity;
import net.mcreator.borninchaosv.entity.DoorKnightNotDespawnEntity;
import net.mcreator.borninchaosv.entity.DreadHoundEntity;
import net.mcreator.borninchaosv.entity.DreadHoundNotDespawnEntity;
import net.mcreator.borninchaosv.entity.FallenChaosKnightEntity;
import net.mcreator.borninchaosv.entity.FelsteedEntity;
import net.mcreator.borninchaosv.entity.FirelightEntity;
import net.mcreator.borninchaosv.entity.FirelightNotDespawnEntity;
import net.mcreator.borninchaosv.entity.GluttonFishEntity;
import net.mcreator.borninchaosv.entity.InfernalSpiritEntity;
import net.mcreator.borninchaosv.entity.IntoxicatindBombProjectileEntity;
import net.mcreator.borninchaosv.entity.KrampusEntity;
import net.mcreator.borninchaosv.entity.KrampusHenchmanEntity;
import net.mcreator.borninchaosv.entity.LifestealerEntity;
import net.mcreator.borninchaosv.entity.LifestealerTrueFormEntity;
import net.mcreator.borninchaosv.entity.LordPumpkinheadEntity;
import net.mcreator.borninchaosv.entity.LordPumpkinheadHeadEntity;
import net.mcreator.borninchaosv.entity.LordPumpkinheadWithoutaHorseEntity;
import net.mcreator.borninchaosv.entity.LordTheHeadlessEntity;
import net.mcreator.borninchaosv.entity.LordsFelsteedEntity;
import net.mcreator.borninchaosv.entity.MaggotEntity;
import net.mcreator.borninchaosv.entity.MissionaryChargeEntity;
import net.mcreator.borninchaosv.entity.MissionaryRaiderEntity;
import net.mcreator.borninchaosv.entity.MissionerEntity;
import net.mcreator.borninchaosv.entity.MonstrousEasterEggProjectileEntity;
import net.mcreator.borninchaosv.entity.MotherSpiderEntity;
import net.mcreator.borninchaosv.entity.MrPumpkinControlledEntity;
import net.mcreator.borninchaosv.entity.MrPumpkinEntity;
import net.mcreator.borninchaosv.entity.MrsPumpkinEntity;
import net.mcreator.borninchaosv.entity.NightmareStalkerEntity;
import net.mcreator.borninchaosv.entity.PhantomBombEntityEntity;
import net.mcreator.borninchaosv.entity.PhantomBombProjectileEntity;
import net.mcreator.borninchaosv.entity.PhantomCreeperCopyEntity;
import net.mcreator.borninchaosv.entity.PhantomCreeperEntity;
import net.mcreator.borninchaosv.entity.PumpkinBombEntity;
import net.mcreator.borninchaosv.entity.PumpkinBruiserEntity;
import net.mcreator.borninchaosv.entity.PumpkinDunceEntity;
import net.mcreator.borninchaosv.entity.PumpkinPistol2ProjectileEntity;
import net.mcreator.borninchaosv.entity.PumpkinPistolProjectileEntity;
import net.mcreator.borninchaosv.entity.PumpkinSpiritEntity;
import net.mcreator.borninchaosv.entity.PumpkinStaff2ProjectileEntity;
import net.mcreator.borninchaosv.entity.PumpkinStaffProjectileEntity;
import net.mcreator.borninchaosv.entity.PumpkinheadEntity;
import net.mcreator.borninchaosv.entity.RestlessSpiritEntity;
import net.mcreator.borninchaosv.entity.RidingFelsteedEntity;
import net.mcreator.borninchaosv.entity.RidingLordsFelsteedEntity;
import net.mcreator.borninchaosv.entity.RottenEasterEggProjectileEntity;
import net.mcreator.borninchaosv.entity.ScarletPersecutorEntity;
import net.mcreator.borninchaosv.entity.SearedSpiritEntity;
import net.mcreator.borninchaosv.entity.SearedSpiritNotDespawnEntity;
import net.mcreator.borninchaosv.entity.SenorPumpkinEntity;
import net.mcreator.borninchaosv.entity.SiameseSkeletonsEntity;
import net.mcreator.borninchaosv.entity.SiameseSkeletonsleftEntity;
import net.mcreator.borninchaosv.entity.SiameseSkeletonsrightEntity;
import net.mcreator.borninchaosv.entity.SirPumpkinheadEntity;
import net.mcreator.borninchaosv.entity.SirPumpkinheadWithoutHorseEntity;
import net.mcreator.borninchaosv.entity.SirTheHeadlessEntity;
import net.mcreator.borninchaosv.entity.SkeletonDemomanEntity;
import net.mcreator.borninchaosv.entity.SkeletonThrasherEntity;
import net.mcreator.borninchaosv.entity.SkeletonThrasherNotDespawnEntity;
import net.mcreator.borninchaosv.entity.SpiritGuideAssistantEntity;
import net.mcreator.borninchaosv.entity.SpiritGuideEntity;
import net.mcreator.borninchaosv.entity.SpiritofChaosEntity;
import net.mcreator.borninchaosv.entity.SpiritualEasterEggProjectileEntity;
import net.mcreator.borninchaosv.entity.StaffofBlindnessProjectileEntity;
import net.mcreator.borninchaosv.entity.StaffofMagicArrows2ProjectileEntity;
import net.mcreator.borninchaosv.entity.StaffofMagicArrowsProjectileEntity;
import net.mcreator.borninchaosv.entity.StimulatingBombprojectileEntity;
import net.mcreator.borninchaosv.entity.SupremeBonescallerEntity;
import net.mcreator.borninchaosv.entity.SupremeBonescallerNotDespawnEntity;
import net.mcreator.borninchaosv.entity.SupremeBonescallerStage2Entity;
import net.mcreator.borninchaosv.entity.SwarmerEntity;
import net.mcreator.borninchaosv.entity.ThornshellCrabEntity;
import net.mcreator.borninchaosv.entity.ZombieBruiserEntity;
import net.mcreator.borninchaosv.entity.ZombieClownEntity;
import net.mcreator.borninchaosv.entity.ZombieClownNotDespawnEntity;
import net.mcreator.borninchaosv.entity.ZombieFishermanEntity;
import net.mcreator.borninchaosv.entity.ZombieLumberjackEntity;
import net.mcreator.borninchaosv.item.BonescallerStaffItem;
import net.mcreator.borninchaosv.item.PumpkinhandgunItem;
import net.mcreator.borninchaosv.item.PumpkinstaffaItem;
import net.mcreator.borninchaosv.item.StaffOfMagicArrowsAItem;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import software.bernie.geckolib.animation.RawAnimation;

/**
 * 为全部 98 种生于混沌实体及 4 种 GeckoLib 动画物品统一缓存：
 * 1. 环境音、脚步声、受击声、死亡声与弹射物音效的 ResourceLocation 解析与注册表查表；
 * 2. GeckoLib 动画谓词每渲染帧重复创建的 RawAnimation 实例。
 */
@Mixin(value = {
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
        IntoxicatindBombProjectileEntity.class,
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
        MissionaryChargeEntity.class,
        MissionaryRaiderEntity.class,
        MissionerEntity.class,
        MonstrousEasterEggProjectileEntity.class,
        MotherSpiderEntity.class,
        MrPumpkinControlledEntity.class,
        MrPumpkinEntity.class,
        MrsPumpkinEntity.class,
        NightmareStalkerEntity.class,
        PhantomBombEntityEntity.class,
        PhantomBombProjectileEntity.class,
        PhantomCreeperCopyEntity.class,
        PhantomCreeperEntity.class,
        PumpkinBombEntity.class,
        PumpkinBruiserEntity.class,
        PumpkinDunceEntity.class,
        PumpkinPistol2ProjectileEntity.class,
        PumpkinPistolProjectileEntity.class,
        PumpkinSpiritEntity.class,
        PumpkinStaff2ProjectileEntity.class,
        PumpkinStaffProjectileEntity.class,
        PumpkinheadEntity.class,
        RestlessSpiritEntity.class,
        RidingFelsteedEntity.class,
        RidingLordsFelsteedEntity.class,
        RottenEasterEggProjectileEntity.class,
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
        SpiritualEasterEggProjectileEntity.class,
        StaffofBlindnessProjectileEntity.class,
        StaffofMagicArrows2ProjectileEntity.class,
        StaffofMagicArrowsProjectileEntity.class,
        StimulatingBombprojectileEntity.class,
        SupremeBonescallerEntity.class,
        SupremeBonescallerNotDespawnEntity.class,
        SupremeBonescallerStage2Entity.class,
        SwarmerEntity.class,
        ThornshellCrabEntity.class,
        ZombieBruiserEntity.class,
        ZombieClownEntity.class,
        ZombieClownNotDespawnEntity.class,
        ZombieFishermanEntity.class,
        ZombieLumberjackEntity.class,
        BonescallerStaffItem.class,
        PumpkinhandgunItem.class,
        PumpkinstaffaItem.class,
        StaffOfMagicArrowsAItem.class
}, remap = false)
public abstract class EntitySoundAndAnimCacheMixin {
    @Redirect(
            method = "*",
            require = 0,
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/resources/ResourceLocation;parse(Ljava/lang/String;)Lnet/minecraft/resources/ResourceLocation;"
            )
    )
    private static ResourceLocation bic$cacheEntityResourceParse(String location) {
        if (!OptimizerConfig.isEnabled(OptimizerConfig.CACHE_RESOURCE_LOCATIONS_AND_SOUNDS)) {
            return ResourceLocation.parse(location);
        }
        return BicResourceAndAnimCache.parseLocation(location);
    }

    @Redirect(
            method = "*",
            require = 0,
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/core/Registry;get(Lnet/minecraft/resources/ResourceLocation;)Ljava/lang/Object;"
            )
    )
    private static <T> T bic$cacheEntitySoundRegistryLookup(Registry<T> registry, ResourceLocation location) {
        if (!OptimizerConfig.isEnabled(OptimizerConfig.CACHE_RESOURCE_LOCATIONS_AND_SOUNDS)) {
            return registry.get(location);
        }
        return BicResourceAndAnimCache.lookupRegistry(registry, location);
    }

    @Redirect(
            method = {"movementPredicate", "attackingPredicate", "procedurePredicate", "idlePredicate"},
            require = 0,
            at = @At(
                    value = "INVOKE",
                    target = "Lsoftware/bernie/geckolib/animation/RawAnimation;begin()Lsoftware/bernie/geckolib/animation/RawAnimation;"
            )
    )
    private RawAnimation bic$beginCachedAnim() {
        if (!OptimizerConfig.isEnabled(OptimizerConfig.CACHE_RAW_ANIMATIONS)) {
            return RawAnimation.begin();
        }
        return BicResourceAndAnimCache.DUMMY_BEGIN_SENTINEL;
    }

    @Redirect(
            method = {"movementPredicate", "attackingPredicate", "procedurePredicate", "idlePredicate"},
            require = 0,
            at = @At(
                    value = "INVOKE",
                    target = "Lsoftware/bernie/geckolib/animation/RawAnimation;thenLoop(Ljava/lang/String;)Lsoftware/bernie/geckolib/animation/RawAnimation;"
            )
    )
    private RawAnimation bic$thenLoopCachedAnim(RawAnimation instance, String animationName) {
        if (!OptimizerConfig.isEnabled(OptimizerConfig.CACHE_RAW_ANIMATIONS)) {
            return (instance == BicResourceAndAnimCache.DUMMY_BEGIN_SENTINEL ? RawAnimation.begin() : instance).thenLoop(animationName);
        }
        return BicResourceAndAnimCache.loopAnim(animationName);
    }

    @Redirect(
            method = {"movementPredicate", "attackingPredicate", "procedurePredicate", "idlePredicate"},
            require = 0,
            at = @At(
                    value = "INVOKE",
                    target = "Lsoftware/bernie/geckolib/animation/RawAnimation;thenPlay(Ljava/lang/String;)Lsoftware/bernie/geckolib/animation/RawAnimation;"
            )
    )
    private RawAnimation bic$thenPlayCachedAnim(RawAnimation instance, String animationName) {
        if (!OptimizerConfig.isEnabled(OptimizerConfig.CACHE_RAW_ANIMATIONS)) {
            return (instance == BicResourceAndAnimCache.DUMMY_BEGIN_SENTINEL ? RawAnimation.begin() : instance).thenPlay(animationName);
        }
        return BicResourceAndAnimCache.playAnim(animationName);
    }
}
