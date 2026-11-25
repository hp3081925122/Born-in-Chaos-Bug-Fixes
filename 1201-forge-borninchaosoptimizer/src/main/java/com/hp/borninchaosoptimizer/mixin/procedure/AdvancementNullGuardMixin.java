package com.hp.borninchaosoptimizer.mixin.procedure;

import com.hp.borninchaosoptimizer.util.BicCrossCheckHelper;
import net.mcreator.borninchaosv.procedures.*;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.server.PlayerAdvancements;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * 为全模组全部 54 个调用 PlayerAdvancements.getOrStartProgress 的过程补充空进度安全兜底，
 * 防止整合包作者通过数据包裁剪或禁用生于混沌进度后，玩家穿戴护甲、合成物品、挖掘矿石或击杀首领时触发空指针崩服。
 */
@Mixin({
        BonescallerPriGibieliSushchnostiProcedure.class,
        BroadswordDestructionPriPoluchieniiPriedmietaPoRietsieptuProcedure.class,
        BroadswordDestructionPriUdariePoSushchnostiInstrumientomProcedure.class,
        DamnedDemomansHatKazhdyiTikDliaShliemaProcedure.class,
        DarkAtriumPriPoluchieniiPriedmietaPoRietsieptuProcedure.class,
        DarkBlacksmithPProcedure.class,
        DarkMetalArmorSobytiieTaktovShliemaProcedure.class,
        DarkMetalIngotPriPoluchieniiPriedmietaPoRietsieptuProcedure.class,
        DarkVortexPriGibieliSushchnostiProcedure.class,
        DireHoundLeaderPriGibieliSushchnostiProcedure.class,
        ElixirofInsectProtectionPriPoluchieniiPriedmietaPoRietsieptuProcedure.class,
        EternalCandyPriPoluchieniiPriedmietaPoRietsieptuProcedure.class,
        EtherealSpiritPriShchielchkiePravoiKnopkoiMyshiNaBlokieProcedure.class,
        GluttonFishPriGibieliSushchnostiProcedure.class,
        GraveDestroyerProcedure.class,
        HoundTrapPriStolknovieniiSushchnostiSBlokomProcedure.class,
        InfectedDiamondOreBrekProcedure.class,
        INSESTProcedure.class,
        KillinganimalsProcedure.class,
        KillingVillagersProcedure.class,
        KrampusPriGibieliSushchnostiProcedure.class,
        LifestealElixirPriPoluchieniiPriedmietaPoRietsieptuProcedure.class,
        LifestealerTrueFormPriGibieliSushchnostiProcedure.class,
        LordPumpkinheadDedProcedure.class,
        LunarSpruceCowboyPProcedure.class,
        MintCandyPriZaviershieniiIspolzovaniiaProcedure.class,
        MissionerPriGibieliSushchnostiProcedure.class,
        MonsterFleshPriUdariePoSushchnostiPriedmietomProcedure.class,
        MotherSpiderPriGibieliSushchnostiProcedure.class,
        NightmareMantleoftheNightKazhdyiTikDliaNaghrudnikaProcedure.class,
        NightmareStalkerPriGibieliSushchnostiProcedure.class,
        ObsessiondeadProcedure.class,
        PhantomBombPriPoluchieniiPriedmietaPoRietsieptuProcedure.class,
        PhantomCreeperKoghdaSushchnostRanienaProcedure.class,
        PumpkinheadKoghdaSushchnostUmiraietProcedure.class,
        RampagePProcedure.class,
        SeaTerrorStomachPriShchielchkiePKMProcedure.class,
        SkeletonDemomanPriGibieliSushchnostiProcedure.class,
        SkullCrPProcedure.class,
        SmokedFleshPriZaviershieniiIspolzovaniiaProcedure.class,
        SpiderBitePriPoluchieniiPriedmietaPoRietsieptuProcedure.class,
        SpinyShellArmorKazhdyiTikDliaShliema2Procedure.class,
        SpiritGuidePriGibieliSushchnostiProcedure.class,
        SpiritualGingerbreadPriZaviershieniiIspolzovaniiaProcedure.class,
        SquireoftheDarkLordPProcedure.class,
        StaffoftheSummonerPriPoluchieniiPriedmietaPoRietsieptuProcedure.class,
        StimulatingBombPriPoluchieniiPriedmietaPoRietsieptuProcedure.class,
        StimulationKazhdyiTikVoVriemiaEffiektaProcedure.class,
        SupremeBonescallerStage2PriGibieliSushchnostiProcedure.class,
        SweetSwordPriPoluchieniiPriedmietaPoRietsieptuProcedure.class,
        ThornshellCrabPriShchielchkiePKMPoSushchnostiProcedure.class,
        TransmutingElixirclicProcedure.class,
        TransmutingElixirPriPoluchieniiPriedmietaPoRietsieptuProcedure.class,
        ZombieClownPriGibieliSushchnostiProcedure.class
})
public abstract class AdvancementNullGuardMixin {
    @Redirect(
            method = "execute*",
            remap = false,
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/PlayerAdvancements;getOrStartProgress(Lnet/minecraft/advancements/Advancement;)Lnet/minecraft/advancements/AdvancementProgress;",
                    remap = true
            ),
            require = 0
    )
    private static AdvancementProgress bic$guardNullAdvancement(PlayerAdvancements playerAdvancements, Advancement advancement) {
        return BicCrossCheckHelper.safeGetOrStartProgress(playerAdvancements, advancement);
    }
}
