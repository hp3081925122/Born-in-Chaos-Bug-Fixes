package com.hp.borninchaosoptimizer.mixin;

import com.hp.borninchaosoptimizer.util.Batch5ProcedureHelper;
import net.mcreator.borninchaosv.procedures.BroadswordDestructionPriPoluchieniiPriedmietaPoRietsieptuProcedure;
import net.mcreator.borninchaosv.procedures.DamnedDemomansHatKazhdyiTikDliaShliemaProcedure;
import net.mcreator.borninchaosv.procedures.DarkBlacksmithPProcedure;
import net.mcreator.borninchaosv.procedures.DarkMetalArmorSobytiieTaktovShliemaProcedure;
import net.mcreator.borninchaosv.procedures.DarkMetalIngotPriPoluchieniiPriedmietaPoRietsieptuProcedure;
import net.mcreator.borninchaosv.procedures.DarkVortexPriGibieliSushchnostiProcedure;
import net.mcreator.borninchaosv.procedures.DireHoundLeaderPriGibieliSushchnostiProcedure;
import net.mcreator.borninchaosv.procedures.INSESTProcedure;
import net.mcreator.borninchaosv.procedures.InfectedDiamondOreBrekProcedure;
import net.mcreator.borninchaosv.procedures.NightmareMantleoftheNightKazhdyiTikDliaNaghrudnikaProcedure;
import net.mcreator.borninchaosv.procedures.NightmareStalkerPriGibieliSushchnostiProcedure;
import net.mcreator.borninchaosv.procedures.PhantomBombPriPoluchieniiPriedmietaPoRietsieptuProcedure;
import net.mcreator.borninchaosv.procedures.PhantomCreeperKoghdaSushchnostRanienaProcedure;
import net.mcreator.borninchaosv.procedures.SkeletonDemomanPriGibieliSushchnostiProcedure;
import net.mcreator.borninchaosv.procedures.SkullCrPProcedure;
import net.mcreator.borninchaosv.procedures.SpiritGuidePriGibieliSushchnostiProcedure;
import net.mcreator.borninchaosv.procedures.SquireoftheDarkLordPProcedure;
import net.mcreator.borninchaosv.procedures.StaffoftheSummonerPriPoluchieniiPriedmietaPoRietsieptuProcedure;
import net.mcreator.borninchaosv.procedures.StimulatingBombPriPoluchieniiPriedmietaPoRietsieptuProcedure;
import net.mcreator.borninchaosv.procedures.StimulationKazhdyiTikVoVriemiaEffiektaProcedure;
import net.mcreator.borninchaosv.procedures.SupremeBonescallerStage2PriGibieliSushchnostiProcedure;
import net.mcreator.borninchaosv.procedures.ZombieClownPriGibieliSushchnostiProcedure;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.server.PlayerAdvancements;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * 为全部 22 个缺失进度判空的过程补充保护，防止整合包通过数据包裁剪进度后穿戴装备、挖掘矿石或击杀怪物时触发空指针崩服。
 */
@Mixin(value = {
        BroadswordDestructionPriPoluchieniiPriedmietaPoRietsieptuProcedure.class,
        DamnedDemomansHatKazhdyiTikDliaShliemaProcedure.class,
        DarkBlacksmithPProcedure.class,
        DarkMetalArmorSobytiieTaktovShliemaProcedure.class,
        DarkMetalIngotPriPoluchieniiPriedmietaPoRietsieptuProcedure.class,
        DarkVortexPriGibieliSushchnostiProcedure.class,
        DireHoundLeaderPriGibieliSushchnostiProcedure.class,
        InfectedDiamondOreBrekProcedure.class,
        INSESTProcedure.class,
        NightmareMantleoftheNightKazhdyiTikDliaNaghrudnikaProcedure.class,
        NightmareStalkerPriGibieliSushchnostiProcedure.class,
        PhantomBombPriPoluchieniiPriedmietaPoRietsieptuProcedure.class,
        PhantomCreeperKoghdaSushchnostRanienaProcedure.class,
        SkeletonDemomanPriGibieliSushchnostiProcedure.class,
        SkullCrPProcedure.class,
        SpiritGuidePriGibieliSushchnostiProcedure.class,
        SquireoftheDarkLordPProcedure.class,
        StaffoftheSummonerPriPoluchieniiPriedmietaPoRietsieptuProcedure.class,
        StimulatingBombPriPoluchieniiPriedmietaPoRietsieptuProcedure.class,
        StimulationKazhdyiTikVoVriemiaEffiektaProcedure.class,
        SupremeBonescallerStage2PriGibieliSushchnostiProcedure.class,
        ZombieClownPriGibieliSushchnostiProcedure.class
}, remap = false)
public abstract class AdvancementNullGuardMixin {
    @Redirect(
            method = "execute",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/PlayerAdvancements;getOrStartProgress(Lnet/minecraft/advancements/AdvancementHolder;)Lnet/minecraft/advancements/AdvancementProgress;"
            ),
            require = 0
    )
    private static AdvancementProgress bic$guardNullAdvancement(PlayerAdvancements advancements, AdvancementHolder holder) {
        return Batch5ProcedureHelper.safeGetOrStartProgress(advancements, holder);
    }
}
