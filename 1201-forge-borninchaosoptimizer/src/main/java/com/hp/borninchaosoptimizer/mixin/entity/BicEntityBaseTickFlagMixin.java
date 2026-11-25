package com.hp.borninchaosoptimizer.mixin.entity;

import com.hp.borninchaosoptimizer.api.BicRefreshGuard;
import net.mcreator.borninchaosv.entity.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 标记 83 个生于混沌 GeoEntity 的 baseTick 执行窗口，供 Entity.refreshDimensions 精确识别并跳过每 tick 冗余重算。
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
public abstract class BicEntityBaseTickFlagMixin {
    @Inject(method = "baseTick", at = @At("HEAD"))
    private void bic$enterBaseTick(CallbackInfo ci) {
        ((BicRefreshGuard) this).bic$setInBicBaseTick(true);
    }

    @Inject(method = "baseTick", at = @At("RETURN"))
    private void bic$exitBaseTick(CallbackInfo ci) {
        ((BicRefreshGuard) this).bic$setInBicBaseTick(false);
    }
}
