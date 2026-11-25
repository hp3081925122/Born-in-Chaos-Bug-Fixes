package com.hp.borninchaosoptimizer.mixin.gecko;

import com.hp.borninchaosoptimizer.api.BicAnimatableEntity;
import net.mcreator.borninchaosv.entity.*;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 为全部 83 个生于混沌 GeoEntity 注入统一的 BicAnimatableEntity 接口，
 * 并修复服务端同 tick 内立即将 ANIMATION 重置回 "undefined" 导致的客户端吞动画竞争 Bug。
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
public abstract class BicAnimatableEntityMixin implements BicAnimatableEntity {
    @Shadow(remap = false)
    public String animationprocedure;

    @Shadow(remap = false)
    public abstract String getSyncedAnimation();

    @Shadow(remap = false)
    public abstract void setAnimation(String animation);

    @Shadow(remap = false)
    public abstract String getTexture();

    @Unique
    private static final ConcurrentHashMap<String, ResourceLocation> BIC_ENTITY_TEX_MAP = new ConcurrentHashMap<>();

    @Unique
    private boolean bic$pendingServerAnimReset = false;

    @Unique
    private String bic$lastTexName;

    @Unique
    private ResourceLocation bic$cachedTexLoc;

    @Override
    public ResourceLocation bic$getCachedTextureLocation() {
        String tex = this.getTexture();
        if (tex == null || tex.isEmpty()) {
            tex = "missing";
        }
        if (tex.equals(this.bic$lastTexName) && this.bic$cachedTexLoc != null) {
            return this.bic$cachedTexLoc;
        }
        ResourceLocation resolved = BIC_ENTITY_TEX_MAP.computeIfAbsent(
                tex,
                k -> Objects.requireNonNullElseGet(
                        ResourceLocation.tryBuild("born_in_chaos_v1", "textures/entities/" + k + ".png"),
                        () -> Objects.requireNonNull(ResourceLocation.tryBuild("minecraft", "missingno"))
                )
        );
        this.bic$lastTexName = tex;
        this.bic$cachedTexLoc = resolved;
        return resolved;
    }

    @Inject(method = "setAnimation", at = @At("HEAD"))
    private void bic$onSetAnimation(String animation, CallbackInfo ci) {
        if (animation != null && !"undefined".equals(animation)) {
            this.bic$pendingServerAnimReset = false;
        }
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
        } else {
            if (!this.bic$pendingServerAnimReset) {
                this.animationprocedure = anim;
                this.bic$pendingServerAnimReset = true;
            } else {
                this.bic$pendingServerAnimReset = false;
                this.setAnimation("undefined");
            }
        }
    }
}
