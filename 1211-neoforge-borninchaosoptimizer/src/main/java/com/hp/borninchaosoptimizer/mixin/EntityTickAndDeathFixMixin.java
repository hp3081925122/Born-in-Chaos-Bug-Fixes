package com.hp.borninchaosoptimizer.mixin;

import com.hp.borninchaosoptimizer.config.OptimizerConfig;
import javax.annotation.Nullable;
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
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * 统一优化与修复原模组全部 83 种生物的三大共性缺陷：
 * 1. 缓存姿态（Pose）与缩放比例（Scale），跳过恒定尺寸生物在 baseTick 中每刻双端无条件调用的 refreshDimensions()；
 * 2. 修复 1.21.1 在 die() 掉落经验后又在 tickDeath() 二次调用 dropExperience(this) 造成的双倍经验掉落；
 * 3. 拦截客户端 tickDeath() 提前本地调用 remove(KILLED) 导致的双端不同步，并在服务端补发原版缺失的死亡烟雾事件 (byte)60。
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
})
public abstract class EntityTickAndDeathFixMixin extends LivingEntity {
    @Unique
    private Pose bic$lastDimensionsPose;

    @Unique
    private float bic$lastDimensionsScale = Float.NaN;

    @Unique
    private boolean bic$xpDroppedOnce = false;

    protected EntityTickAndDeathFixMixin(EntityType<? extends LivingEntity> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    public void refreshDimensions() {
        if (!OptimizerConfig.isEnabled(OptimizerConfig.OPTIMIZE_CONSTANT_ENTITY_DIMENSIONS)) {
            super.refreshDimensions();
            return;
        }
        Pose currentPose = this.getPose();
        float currentScale = this.getScale();
        if (this.firstTick || currentPose != this.bic$lastDimensionsPose || currentScale != this.bic$lastDimensionsScale) {
            this.bic$lastDimensionsPose = currentPose;
            this.bic$lastDimensionsScale = currentScale;
            super.refreshDimensions();
        }
    }

    @Override
    protected void dropExperience(@Nullable Entity attacker) {
        if (OptimizerConfig.isEnabled(OptimizerConfig.FIX_DOUBLE_DEATH_EXPERIENCE)) {
            if (this.bic$xpDroppedOnce) {
                return;
            }
            this.bic$xpDroppedOnce = true;
        }
        super.dropExperience(attacker);
    }

    @Override
    public void remove(Entity.RemovalReason reason) {
        if (OptimizerConfig.isEnabled(OptimizerConfig.FIX_DOUBLE_DEATH_EXPERIENCE) && reason == Entity.RemovalReason.KILLED) {
            if (this.level().isClientSide()) {
                return;
            }
            if (!this.isRemoved() && this.deathTime > 0) {
                this.level().broadcastEntityEvent(this, (byte) 60);
            }
        }
        super.remove(reason);
    }
}
