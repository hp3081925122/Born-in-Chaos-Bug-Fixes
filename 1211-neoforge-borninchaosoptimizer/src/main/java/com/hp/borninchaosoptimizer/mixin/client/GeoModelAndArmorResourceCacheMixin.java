package com.hp.borninchaosoptimizer.mixin.client;

import com.hp.borninchaosoptimizer.config.OptimizerConfig;
import com.hp.borninchaosoptimizer.util.BicResourceAndAnimCache;
import net.mcreator.borninchaosv.entity.model.BabySkeletonMinionModel;
import net.mcreator.borninchaosv.entity.model.BabySkeletonModel;
import net.mcreator.borninchaosv.entity.model.BabySpiderControlledModel;
import net.mcreator.borninchaosv.entity.model.BabySpiderModel;
import net.mcreator.borninchaosv.entity.model.BarrelZombieModel;
import net.mcreator.borninchaosv.entity.model.BloodyGadflyModel;
import net.mcreator.borninchaosv.entity.model.BoneImpMinionModel;
import net.mcreator.borninchaosv.entity.model.BoneImpModel;
import net.mcreator.borninchaosv.entity.model.BonescallerModel;
import net.mcreator.borninchaosv.entity.model.BonescallerNotDespawnModel;
import net.mcreator.borninchaosv.entity.model.ControlledBabySkeletonModel;
import net.mcreator.borninchaosv.entity.model.ControlledSpiritualAssistantModel;
import net.mcreator.borninchaosv.entity.model.CorpseFishModel;
import net.mcreator.borninchaosv.entity.model.CorpseFlyModel;
import net.mcreator.borninchaosv.entity.model.DarkVortexModel;
import net.mcreator.borninchaosv.entity.model.DecayingZombieModel;
import net.mcreator.borninchaosv.entity.model.DecayingZombieNotDespawnModel;
import net.mcreator.borninchaosv.entity.model.DecrepitSkeletonModel;
import net.mcreator.borninchaosv.entity.model.DiamondThermiteModel;
import net.mcreator.borninchaosv.entity.model.DireHoundLeaderModel;
import net.mcreator.borninchaosv.entity.model.DoorKnightModel;
import net.mcreator.borninchaosv.entity.model.DoorKnightNotDespawnModel;
import net.mcreator.borninchaosv.entity.model.DreadHoundModel;
import net.mcreator.borninchaosv.entity.model.DreadHoundNotDespawnModel;
import net.mcreator.borninchaosv.entity.model.FallenChaosKnightModel;
import net.mcreator.borninchaosv.entity.model.FelsteedModel;
import net.mcreator.borninchaosv.entity.model.FirelightModel;
import net.mcreator.borninchaosv.entity.model.FirelightNotDespawnModel;
import net.mcreator.borninchaosv.entity.model.GluttonFishModel;
import net.mcreator.borninchaosv.entity.model.InfernalSpiritModel;
import net.mcreator.borninchaosv.entity.model.KrampusHenchmanModel;
import net.mcreator.borninchaosv.entity.model.KrampusModel;
import net.mcreator.borninchaosv.entity.model.LifestealerModel;
import net.mcreator.borninchaosv.entity.model.LifestealerTrueFormModel;
import net.mcreator.borninchaosv.entity.model.LordPumpkinheadHeadModel;
import net.mcreator.borninchaosv.entity.model.LordPumpkinheadModel;
import net.mcreator.borninchaosv.entity.model.LordPumpkinheadWithoutaHorseModel;
import net.mcreator.borninchaosv.entity.model.LordTheHeadlessModel;
import net.mcreator.borninchaosv.entity.model.LordsFelsteedModel;
import net.mcreator.borninchaosv.entity.model.MaggotModel;
import net.mcreator.borninchaosv.entity.model.MissionaryRaiderModel;
import net.mcreator.borninchaosv.entity.model.MissionerModel;
import net.mcreator.borninchaosv.entity.model.MotherSpiderModel;
import net.mcreator.borninchaosv.entity.model.MrPumpkinControlledModel;
import net.mcreator.borninchaosv.entity.model.MrPumpkinModel;
import net.mcreator.borninchaosv.entity.model.MrsPumpkinModel;
import net.mcreator.borninchaosv.entity.model.NightmareStalkerModel;
import net.mcreator.borninchaosv.entity.model.PhantomCreeperCopyModel;
import net.mcreator.borninchaosv.entity.model.PhantomCreeperModel;
import net.mcreator.borninchaosv.entity.model.PumpkinBombModel;
import net.mcreator.borninchaosv.entity.model.PumpkinBruiserModel;
import net.mcreator.borninchaosv.entity.model.PumpkinDunceModel;
import net.mcreator.borninchaosv.entity.model.PumpkinSpiritModel;
import net.mcreator.borninchaosv.entity.model.PumpkinheadModel;
import net.mcreator.borninchaosv.entity.model.RestlessSpiritModel;
import net.mcreator.borninchaosv.entity.model.RidingFelsteedModel;
import net.mcreator.borninchaosv.entity.model.RidingLordsFelsteedModel;
import net.mcreator.borninchaosv.entity.model.ScarletPersecutorModel;
import net.mcreator.borninchaosv.entity.model.SearedSpiritModel;
import net.mcreator.borninchaosv.entity.model.SearedSpiritNotDespawnModel;
import net.mcreator.borninchaosv.entity.model.SenorPumpkinModel;
import net.mcreator.borninchaosv.entity.model.SiameseSkeletonsModel;
import net.mcreator.borninchaosv.entity.model.SiameseSkeletonsleftModel;
import net.mcreator.borninchaosv.entity.model.SiameseSkeletonsrightModel;
import net.mcreator.borninchaosv.entity.model.SirPumpkinheadModel;
import net.mcreator.borninchaosv.entity.model.SirPumpkinheadWithoutHorseModel;
import net.mcreator.borninchaosv.entity.model.SirTheHeadlessModel;
import net.mcreator.borninchaosv.entity.model.SkeletonDemomanModel;
import net.mcreator.borninchaosv.entity.model.SkeletonThrasherModel;
import net.mcreator.borninchaosv.entity.model.SkeletonThrasherNotDespawnModel;
import net.mcreator.borninchaosv.entity.model.SpiritGuideAssistantModel;
import net.mcreator.borninchaosv.entity.model.SpiritGuideModel;
import net.mcreator.borninchaosv.entity.model.SpiritofChaosModel;
import net.mcreator.borninchaosv.entity.model.SupremeBonescallerModel;
import net.mcreator.borninchaosv.entity.model.SupremeBonescallerNotDespawnModel;
import net.mcreator.borninchaosv.entity.model.SupremeBonescallerStage2Model;
import net.mcreator.borninchaosv.entity.model.SwarmerModel;
import net.mcreator.borninchaosv.entity.model.ThornshellCrabModel;
import net.mcreator.borninchaosv.entity.model.ZombieBruiserModel;
import net.mcreator.borninchaosv.entity.model.ZombieClownModel;
import net.mcreator.borninchaosv.entity.model.ZombieClownNotDespawnModel;
import net.mcreator.borninchaosv.entity.model.ZombieFishermanModel;
import net.mcreator.borninchaosv.entity.model.ZombieLumberjackModel;
import net.mcreator.borninchaosv.item.DamnedDemomansHatItem;
import net.mcreator.borninchaosv.item.DarkMetalArmorItem;
import net.mcreator.borninchaosv.item.KillerRabbitEarsItem;
import net.mcreator.borninchaosv.item.LordPumpkinheadsHatItem;
import net.mcreator.borninchaosv.item.MissionaryHatItem;
import net.mcreator.borninchaosv.item.NightmareMantleoftheNightItem;
import net.mcreator.borninchaosv.item.SpinyShellArmorItem;
import net.mcreator.borninchaosv.item.SpiritualGuideSombreroItem;
import net.mcreator.borninchaosv.item.model.BonescallerStaffItemModel;
import net.mcreator.borninchaosv.item.model.PumpkinhandgunItemModel;
import net.mcreator.borninchaosv.item.model.PumpkinstaffaItemModel;
import net.mcreator.borninchaosv.item.model.StaffOfMagicArrowsAItemModel;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * 为全部 83 种生物 GeoModel、4 种物品 GeoModel 以及 15 种自定义盔甲子类统一缓存 ResourceLocation.parse 调用，
 * 消除每渲染帧在 getAnimationResource、getModelResource、getTextureResource 与 getArmorTexture 中的重复字符串解析。
 */
@Mixin(value = {
        BabySkeletonMinionModel.class,
        BabySkeletonModel.class,
        BabySpiderControlledModel.class,
        BabySpiderModel.class,
        BarrelZombieModel.class,
        BloodyGadflyModel.class,
        BoneImpMinionModel.class,
        BoneImpModel.class,
        BonescallerModel.class,
        BonescallerNotDespawnModel.class,
        ControlledBabySkeletonModel.class,
        ControlledSpiritualAssistantModel.class,
        CorpseFishModel.class,
        CorpseFlyModel.class,
        DarkVortexModel.class,
        DecayingZombieModel.class,
        DecayingZombieNotDespawnModel.class,
        DecrepitSkeletonModel.class,
        DiamondThermiteModel.class,
        DireHoundLeaderModel.class,
        DoorKnightModel.class,
        DoorKnightNotDespawnModel.class,
        DreadHoundModel.class,
        DreadHoundNotDespawnModel.class,
        FallenChaosKnightModel.class,
        FelsteedModel.class,
        FirelightModel.class,
        FirelightNotDespawnModel.class,
        GluttonFishModel.class,
        InfernalSpiritModel.class,
        KrampusHenchmanModel.class,
        KrampusModel.class,
        LifestealerModel.class,
        LifestealerTrueFormModel.class,
        LordPumpkinheadHeadModel.class,
        LordPumpkinheadModel.class,
        LordPumpkinheadWithoutaHorseModel.class,
        LordTheHeadlessModel.class,
        LordsFelsteedModel.class,
        MaggotModel.class,
        MissionaryRaiderModel.class,
        MissionerModel.class,
        MotherSpiderModel.class,
        MrPumpkinControlledModel.class,
        MrPumpkinModel.class,
        MrsPumpkinModel.class,
        NightmareStalkerModel.class,
        PhantomCreeperCopyModel.class,
        PhantomCreeperModel.class,
        PumpkinBombModel.class,
        PumpkinBruiserModel.class,
        PumpkinDunceModel.class,
        PumpkinSpiritModel.class,
        PumpkinheadModel.class,
        RestlessSpiritModel.class,
        RidingFelsteedModel.class,
        RidingLordsFelsteedModel.class,
        ScarletPersecutorModel.class,
        SearedSpiritModel.class,
        SearedSpiritNotDespawnModel.class,
        SenorPumpkinModel.class,
        SiameseSkeletonsModel.class,
        SiameseSkeletonsleftModel.class,
        SiameseSkeletonsrightModel.class,
        SirPumpkinheadModel.class,
        SirPumpkinheadWithoutHorseModel.class,
        SirTheHeadlessModel.class,
        SkeletonDemomanModel.class,
        SkeletonThrasherModel.class,
        SkeletonThrasherNotDespawnModel.class,
        SpiritGuideAssistantModel.class,
        SpiritGuideModel.class,
        SpiritofChaosModel.class,
        SupremeBonescallerModel.class,
        SupremeBonescallerNotDespawnModel.class,
        SupremeBonescallerStage2Model.class,
        SwarmerModel.class,
        ThornshellCrabModel.class,
        ZombieBruiserModel.class,
        ZombieClownModel.class,
        ZombieClownNotDespawnModel.class,
        ZombieFishermanModel.class,
        ZombieLumberjackModel.class,
        BonescallerStaffItemModel.class,
        PumpkinhandgunItemModel.class,
        PumpkinstaffaItemModel.class,
        StaffOfMagicArrowsAItemModel.class,
        DamnedDemomansHatItem.Helmet.class,
        DarkMetalArmorItem.Boots.class,
        DarkMetalArmorItem.Chestplate.class,
        DarkMetalArmorItem.Helmet.class,
        DarkMetalArmorItem.Leggings.class,
        KillerRabbitEarsItem.Helmet.class,
        LordPumpkinheadsHatItem.Helmet.class,
        MissionaryHatItem.Helmet.class,
        NightmareMantleoftheNightItem.Boots.class,
        NightmareMantleoftheNightItem.Chestplate.class,
        NightmareMantleoftheNightItem.Helmet.class,
        NightmareMantleoftheNightItem.Leggings.class,
        SpinyShellArmorItem.Chestplate.class,
        SpinyShellArmorItem.Helmet.class,
        SpiritualGuideSombreroItem.Helmet.class
}, remap = false)
public abstract class GeoModelAndArmorResourceCacheMixin {
    @Redirect(
            method = "*",
            require = 0,
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/resources/ResourceLocation;parse(Ljava/lang/String;)Lnet/minecraft/resources/ResourceLocation;"
            )
    )
    private static ResourceLocation bic$cacheModelAndArmorParse(String location) {
        if (!OptimizerConfig.isEnabled(OptimizerConfig.CACHE_RESOURCE_LOCATIONS_AND_SOUNDS)) {
            return ResourceLocation.parse(location);
        }
        return BicResourceAndAnimCache.parseLocation(location);
    }
}
