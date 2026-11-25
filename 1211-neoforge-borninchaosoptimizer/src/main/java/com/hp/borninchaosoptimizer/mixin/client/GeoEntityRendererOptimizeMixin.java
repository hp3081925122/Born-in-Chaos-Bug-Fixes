package com.hp.borninchaosoptimizer.mixin.client;

import com.hp.borninchaosoptimizer.client.ClientRenderCache;
import com.hp.borninchaosoptimizer.config.OptimizerConfig;
import com.hp.borninchaosoptimizer.util.IBicSyncedAnimatable;
import net.mcreator.borninchaosv.client.renderer.BabySkeletonMinionRenderer;
import net.mcreator.borninchaosv.client.renderer.BabySkeletonRenderer;
import net.mcreator.borninchaosv.client.renderer.BabySpiderControlledRenderer;
import net.mcreator.borninchaosv.client.renderer.BabySpiderRenderer;
import net.mcreator.borninchaosv.client.renderer.BarrelZombieRenderer;
import net.mcreator.borninchaosv.client.renderer.BloodyGadflyRenderer;
import net.mcreator.borninchaosv.client.renderer.BoneImpMinionRenderer;
import net.mcreator.borninchaosv.client.renderer.BoneImpRenderer;
import net.mcreator.borninchaosv.client.renderer.BonescallerNotDespawnRenderer;
import net.mcreator.borninchaosv.client.renderer.BonescallerRenderer;
import net.mcreator.borninchaosv.client.renderer.ControlledBabySkeletonRenderer;
import net.mcreator.borninchaosv.client.renderer.ControlledSpiritualAssistantRenderer;
import net.mcreator.borninchaosv.client.renderer.CorpseFishRenderer;
import net.mcreator.borninchaosv.client.renderer.CorpseFlyRenderer;
import net.mcreator.borninchaosv.client.renderer.DarkVortexRenderer;
import net.mcreator.borninchaosv.client.renderer.DecayingZombieNotDespawnRenderer;
import net.mcreator.borninchaosv.client.renderer.DecayingZombieRenderer;
import net.mcreator.borninchaosv.client.renderer.DecrepitSkeletonRenderer;
import net.mcreator.borninchaosv.client.renderer.DiamondThermiteRenderer;
import net.mcreator.borninchaosv.client.renderer.DireHoundLeaderRenderer;
import net.mcreator.borninchaosv.client.renderer.DoorKnightNotDespawnRenderer;
import net.mcreator.borninchaosv.client.renderer.DoorKnightRenderer;
import net.mcreator.borninchaosv.client.renderer.DreadHoundNotDespawnRenderer;
import net.mcreator.borninchaosv.client.renderer.DreadHoundRenderer;
import net.mcreator.borninchaosv.client.renderer.FallenChaosKnightRenderer;
import net.mcreator.borninchaosv.client.renderer.FelsteedRenderer;
import net.mcreator.borninchaosv.client.renderer.FirelightNotDespawnRenderer;
import net.mcreator.borninchaosv.client.renderer.FirelightRenderer;
import net.mcreator.borninchaosv.client.renderer.GluttonFishRenderer;
import net.mcreator.borninchaosv.client.renderer.InfernalSpiritRenderer;
import net.mcreator.borninchaosv.client.renderer.KrampusHenchmanRenderer;
import net.mcreator.borninchaosv.client.renderer.KrampusRenderer;
import net.mcreator.borninchaosv.client.renderer.LifestealerRenderer;
import net.mcreator.borninchaosv.client.renderer.LifestealerTrueFormRenderer;
import net.mcreator.borninchaosv.client.renderer.LordPumpkinheadHeadRenderer;
import net.mcreator.borninchaosv.client.renderer.LordPumpkinheadRenderer;
import net.mcreator.borninchaosv.client.renderer.LordPumpkinheadWithoutaHorseRenderer;
import net.mcreator.borninchaosv.client.renderer.LordTheHeadlessRenderer;
import net.mcreator.borninchaosv.client.renderer.LordsFelsteedRenderer;
import net.mcreator.borninchaosv.client.renderer.MaggotRenderer;
import net.mcreator.borninchaosv.client.renderer.MissionaryRaiderRenderer;
import net.mcreator.borninchaosv.client.renderer.MissionerRenderer;
import net.mcreator.borninchaosv.client.renderer.MotherSpiderRenderer;
import net.mcreator.borninchaosv.client.renderer.MrPumpkinControlledRenderer;
import net.mcreator.borninchaosv.client.renderer.MrPumpkinRenderer;
import net.mcreator.borninchaosv.client.renderer.MrsPumpkinRenderer;
import net.mcreator.borninchaosv.client.renderer.NightmareStalkerRenderer;
import net.mcreator.borninchaosv.client.renderer.PhantomCreeperCopyRenderer;
import net.mcreator.borninchaosv.client.renderer.PhantomCreeperRenderer;
import net.mcreator.borninchaosv.client.renderer.PumpkinBombRenderer;
import net.mcreator.borninchaosv.client.renderer.PumpkinBruiserRenderer;
import net.mcreator.borninchaosv.client.renderer.PumpkinDunceRenderer;
import net.mcreator.borninchaosv.client.renderer.PumpkinSpiritRenderer;
import net.mcreator.borninchaosv.client.renderer.PumpkinheadRenderer;
import net.mcreator.borninchaosv.client.renderer.RestlessSpiritRenderer;
import net.mcreator.borninchaosv.client.renderer.RidingFelsteedRenderer;
import net.mcreator.borninchaosv.client.renderer.RidingLordsFelsteedRenderer;
import net.mcreator.borninchaosv.client.renderer.ScarletPersecutorRenderer;
import net.mcreator.borninchaosv.client.renderer.SearedSpiritNotDespawnRenderer;
import net.mcreator.borninchaosv.client.renderer.SearedSpiritRenderer;
import net.mcreator.borninchaosv.client.renderer.SenorPumpkinRenderer;
import net.mcreator.borninchaosv.client.renderer.SiameseSkeletonsRenderer;
import net.mcreator.borninchaosv.client.renderer.SiameseSkeletonsleftRenderer;
import net.mcreator.borninchaosv.client.renderer.SiameseSkeletonsrightRenderer;
import net.mcreator.borninchaosv.client.renderer.SirPumpkinheadRenderer;
import net.mcreator.borninchaosv.client.renderer.SirPumpkinheadWithoutHorseRenderer;
import net.mcreator.borninchaosv.client.renderer.SirTheHeadlessRenderer;
import net.mcreator.borninchaosv.client.renderer.SkeletonDemomanRenderer;
import net.mcreator.borninchaosv.client.renderer.SkeletonThrasherNotDespawnRenderer;
import net.mcreator.borninchaosv.client.renderer.SkeletonThrasherRenderer;
import net.mcreator.borninchaosv.client.renderer.SpiritGuideAssistantRenderer;
import net.mcreator.borninchaosv.client.renderer.SpiritGuideRenderer;
import net.mcreator.borninchaosv.client.renderer.SpiritofChaosRenderer;
import net.mcreator.borninchaosv.client.renderer.SupremeBonescallerNotDespawnRenderer;
import net.mcreator.borninchaosv.client.renderer.SupremeBonescallerRenderer;
import net.mcreator.borninchaosv.client.renderer.SupremeBonescallerStage2Renderer;
import net.mcreator.borninchaosv.client.renderer.SwarmerRenderer;
import net.mcreator.borninchaosv.client.renderer.ThornshellCrabRenderer;
import net.mcreator.borninchaosv.client.renderer.ZombieBruiserRenderer;
import net.mcreator.borninchaosv.client.renderer.ZombieClownNotDespawnRenderer;
import net.mcreator.borninchaosv.client.renderer.ZombieClownRenderer;
import net.mcreator.borninchaosv.client.renderer.ZombieFishermanRenderer;
import net.mcreator.borninchaosv.client.renderer.ZombieLumberjackRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

/**
 * 优化全部 83 种 GeckoLib 生物渲染器：
 * 1. 直接将实体贴图键名通过一槽快速缓存映射至 ResourceLocation，跳过 GeoModel 每帧字符串拼接与重复解析；
 * 2. 缓存 RenderType.entityTranslucent(texture)，消除每实体每帧分配 Pair 键对象的开销。
 */
@Mixin(value = {
        BabySkeletonMinionRenderer.class,
        BabySkeletonRenderer.class,
        BabySpiderControlledRenderer.class,
        BabySpiderRenderer.class,
        BarrelZombieRenderer.class,
        BloodyGadflyRenderer.class,
        BoneImpMinionRenderer.class,
        BoneImpRenderer.class,
        BonescallerNotDespawnRenderer.class,
        BonescallerRenderer.class,
        ControlledBabySkeletonRenderer.class,
        ControlledSpiritualAssistantRenderer.class,
        CorpseFishRenderer.class,
        CorpseFlyRenderer.class,
        DarkVortexRenderer.class,
        DecayingZombieNotDespawnRenderer.class,
        DecayingZombieRenderer.class,
        DecrepitSkeletonRenderer.class,
        DiamondThermiteRenderer.class,
        DireHoundLeaderRenderer.class,
        DoorKnightNotDespawnRenderer.class,
        DoorKnightRenderer.class,
        DreadHoundNotDespawnRenderer.class,
        DreadHoundRenderer.class,
        FallenChaosKnightRenderer.class,
        FelsteedRenderer.class,
        FirelightNotDespawnRenderer.class,
        FirelightRenderer.class,
        GluttonFishRenderer.class,
        InfernalSpiritRenderer.class,
        KrampusHenchmanRenderer.class,
        KrampusRenderer.class,
        LifestealerRenderer.class,
        LifestealerTrueFormRenderer.class,
        LordPumpkinheadHeadRenderer.class,
        LordPumpkinheadRenderer.class,
        LordPumpkinheadWithoutaHorseRenderer.class,
        LordTheHeadlessRenderer.class,
        LordsFelsteedRenderer.class,
        MaggotRenderer.class,
        MissionaryRaiderRenderer.class,
        MissionerRenderer.class,
        MotherSpiderRenderer.class,
        MrPumpkinControlledRenderer.class,
        MrPumpkinRenderer.class,
        MrsPumpkinRenderer.class,
        NightmareStalkerRenderer.class,
        PhantomCreeperCopyRenderer.class,
        PhantomCreeperRenderer.class,
        PumpkinBombRenderer.class,
        PumpkinBruiserRenderer.class,
        PumpkinDunceRenderer.class,
        PumpkinSpiritRenderer.class,
        PumpkinheadRenderer.class,
        RestlessSpiritRenderer.class,
        RidingFelsteedRenderer.class,
        RidingLordsFelsteedRenderer.class,
        ScarletPersecutorRenderer.class,
        SearedSpiritNotDespawnRenderer.class,
        SearedSpiritRenderer.class,
        SenorPumpkinRenderer.class,
        SiameseSkeletonsRenderer.class,
        SiameseSkeletonsleftRenderer.class,
        SiameseSkeletonsrightRenderer.class,
        SirPumpkinheadRenderer.class,
        SirPumpkinheadWithoutHorseRenderer.class,
        SirTheHeadlessRenderer.class,
        SkeletonDemomanRenderer.class,
        SkeletonThrasherNotDespawnRenderer.class,
        SkeletonThrasherRenderer.class,
        SpiritGuideAssistantRenderer.class,
        SpiritGuideRenderer.class,
        SpiritofChaosRenderer.class,
        SupremeBonescallerNotDespawnRenderer.class,
        SupremeBonescallerRenderer.class,
        SupremeBonescallerStage2Renderer.class,
        SwarmerRenderer.class,
        ThornshellCrabRenderer.class,
        ZombieBruiserRenderer.class,
        ZombieClownNotDespawnRenderer.class,
        ZombieClownRenderer.class,
        ZombieFishermanRenderer.class,
        ZombieLumberjackRenderer.class
}, remap = false)
public abstract class GeoEntityRendererOptimizeMixin<T extends Entity & GeoAnimatable> extends GeoEntityRenderer<T> {
    @Unique
    private String bic$lastTextureName;
    @Unique
    private ResourceLocation bic$lastTextureLocation;
    @Unique
    private ResourceLocation bic$lastRenderTypeKey;
    @Unique
    private boolean bic$lastInvisibleState;
    @Unique
    private boolean bic$lastOpaqueConfigState;
    @Unique
    private int bic$lastRenderTypeEpoch = -1;
    @Unique
    private RenderType bic$lastResolvedRenderType;

    protected GeoEntityRendererOptimizeMixin(EntityRendererProvider.Context renderManager, GeoModel<T> model) {
        super(renderManager, model);
    }

    @Override
    public ResourceLocation getTextureLocation(T animatable) {
        if (OptimizerConfig.isEnabled(OptimizerConfig.CACHE_RESOURCE_LOCATIONS_AND_SOUNDS)
                && animatable instanceof IBicSyncedAnimatable synced) {
            String tex = synced.getTexture();
            if (tex != null) {
                if (tex.equals(this.bic$lastTextureName) && this.bic$lastTextureLocation != null) {
                    return this.bic$lastTextureLocation;
                }
                ResourceLocation resolved = ClientRenderCache.entityTexture(tex);
                this.bic$lastTextureName = tex;
                this.bic$lastTextureLocation = resolved;
                return resolved;
            }
        }
        return super.getTextureLocation(animatable);
    }

    @Redirect(
            method = "getRenderType*",
            require = 0,
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/RenderType;entityTranslucent(Lnet/minecraft/resources/ResourceLocation;)Lnet/minecraft/client/renderer/RenderType;"
            )
    )
    private RenderType bic$cacheEntityTranslucent(ResourceLocation texture) {
        if (!OptimizerConfig.isEnabled(OptimizerConfig.OPTIMIZE_GLOW_LAYERS_AND_RENDER_TYPES)) {
            return RenderType.entityTranslucent(texture);
        }
        T current = this.getAnimatable();
        boolean invisible = current != null && current.isInvisible();
        boolean opaqueOpt = OptimizerConfig.isEnabled(OptimizerConfig.OPTIMIZE_OPAQUE_ENTITY_RENDER_TYPES);
        int epoch = ClientRenderCache.getReloadEpoch();
        if (texture != null
                && texture == this.bic$lastRenderTypeKey
                && invisible == this.bic$lastInvisibleState
                && opaqueOpt == this.bic$lastOpaqueConfigState
                && epoch == this.bic$lastRenderTypeEpoch
                && this.bic$lastResolvedRenderType != null) {
            return this.bic$lastResolvedRenderType;
        }
        RenderType resolved = ClientRenderCache.resolveEntityRenderType(current, texture);
        this.bic$lastRenderTypeKey = texture;
        this.bic$lastInvisibleState = invisible;
        this.bic$lastOpaqueConfigState = opaqueOpt;
        this.bic$lastRenderTypeEpoch = epoch;
        this.bic$lastResolvedRenderType = resolved;
        return resolved;
    }
}
