package com.hp.borninchaosoptimizer.mixin;

import com.hp.borninchaosoptimizer.util.IBicSyncedAnimatable;
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
import net.mcreator.borninchaosv.entity.MissionaryRaiderEntity;
import net.mcreator.borninchaosv.entity.MissionerEntity;
import net.mcreator.borninchaosv.entity.MotherSpiderEntity;
import net.mcreator.borninchaosv.entity.MrPumpkinControlledEntity;
import net.mcreator.borninchaosv.entity.MrPumpkinEntity;
import net.mcreator.borninchaosv.entity.MrsPumpkinEntity;
import net.mcreator.borninchaosv.entity.NightmareStalkerEntity;
import net.mcreator.borninchaosv.entity.PhantomCreeperCopyEntity;
import net.mcreator.borninchaosv.entity.PhantomCreeperEntity;
import net.mcreator.borninchaosv.entity.PumpkinBombEntity;
import net.mcreator.borninchaosv.entity.PumpkinBruiserEntity;
import net.mcreator.borninchaosv.entity.PumpkinDunceEntity;
import net.mcreator.borninchaosv.entity.PumpkinSpiritEntity;
import net.mcreator.borninchaosv.entity.PumpkinheadEntity;
import net.mcreator.borninchaosv.entity.RestlessSpiritEntity;
import net.mcreator.borninchaosv.entity.RidingFelsteedEntity;
import net.mcreator.borninchaosv.entity.RidingLordsFelsteedEntity;
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
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 为原模组全部 83 种同步动画实体统一实现 IBicSyncedAnimatable 接口。
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
}, remap = false)
public abstract class SyncedAnimatableEntitiesMixin implements IBicSyncedAnimatable {
    @Shadow(remap = false)
    public String animationprocedure;

    @Unique
    private boolean bic$pendingServerAnimReset = false;

    @Inject(method = "setAnimation", at = @At("HEAD"), remap = false)
    private void bic$onSetAnimation(String animation, CallbackInfo ci) {
        if (animation != null && !"undefined".equals(animation)) {
            this.bic$pendingServerAnimReset = false;
        }
    }

    @Override
    public void bic$setAnimationProcedure(String animation) {
        this.animationprocedure = animation;
    }

    @Override
    public void bic$handleAnimationSyncTick(boolean clientSide) {
        String anim = this.getSyncedAnimation();
        if (anim == null || "undefined".equals(anim)) {
            this.bic$pendingServerAnimReset = false;
            return;
        }
        if (clientSide) {
            this.setAnimation("undefined");
            this.animationprocedure = anim;
        } else if (!this.bic$pendingServerAnimReset) {
            // 服务端保留 1 个完整游戏刻供刻末 ServerEntity.sendChanges() 下发同步包，防止同刻触发的格挡/攻击动画被吞
            this.animationprocedure = anim;
            this.bic$pendingServerAnimReset = true;
        } else {
            this.bic$pendingServerAnimReset = false;
            this.setAnimation("undefined");
        }
    }
}
