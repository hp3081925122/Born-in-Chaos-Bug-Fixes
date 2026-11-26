package com.hp.borninchaosoptimizer.mixin.core;

import com.hp.borninchaosoptimizer.config.BicOptimizerConfig;
import com.hp.borninchaosoptimizer.util.BicPlayerVariablesHelper;
import net.mcreator.borninchaosv.network.BornInChaosV1ModVariables;
import net.mcreator.borninchaosv.procedures.BirchBranchesPriUdariePoSushchnostiInstrumientomProcedure;
import net.mcreator.borninchaosv.procedures.CreepyCookiesWithMilkPriZaviershieniiIspolzovaniiaProcedure;
import net.mcreator.borninchaosv.procedures.CreepyGiftPriShchielchkiePKMProcedure;
import net.mcreator.borninchaosv.procedures.EvilometerKazhdyiTikVInvientarieProcedure;
import net.mcreator.borninchaosv.procedures.EvilometerPriShchielchkiePKMProcedure;
import net.mcreator.borninchaosv.procedures.EvilometerZnachieniieSvoistva2Procedure;
import net.mcreator.borninchaosv.procedures.EvilometerZnachieniieSvoistva3Procedure;
import net.mcreator.borninchaosv.procedures.EvilometerZnachieniieSvoistva4Procedure;
import net.mcreator.borninchaosv.procedures.EvilometerZnachieniieSvoistvaProcedure;
import net.mcreator.borninchaosv.procedures.KillingVillagersProcedure;
import net.mcreator.borninchaosv.procedures.KillinganimalsProcedure;
import net.mcreator.borninchaosv.procedures.PositiveFertilizerProcedure;
import net.mcreator.borninchaosv.procedures.PositiveTamingProcedure;
import net.mcreator.borninchaosv.procedures.SpiritualGingerbreadPriZaviershieniiIspolzovaniiaProcedure;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * 消除 1.20.1 Forge MCreator 模板在全部 14 个过程中共 70 处 .orElse(new PlayerVariables()) 只读回退产生的无效对象分配，
 * 尤其是邪恶仪（Evilometer）客户端每帧渲染谓词与背包每刻检测带来的短命 GC 抖动。
 * 使用 require = 0 以兼容 MCreatorMemFix 等在 ModLauncher Phase.BEFORE 阶段已提前将 .orElse(new PlayerVariables())
 * 改写为 .orElseGet(...) 的优化模组，或覆盖同类淘气值过程的配置模组：对方接管时自动静默跳过而不崩溃，
 * 未安装此类模组（或其首次启动尚未生成配置）时仍全量接管生效。
 */
@Mixin(value = {
        BirchBranchesPriUdariePoSushchnostiInstrumientomProcedure.class,
        CreepyCookiesWithMilkPriZaviershieniiIspolzovaniiaProcedure.class,
        CreepyGiftPriShchielchkiePKMProcedure.class,
        EvilometerKazhdyiTikVInvientarieProcedure.class,
        EvilometerPriShchielchkiePKMProcedure.class,
        EvilometerZnachieniieSvoistvaProcedure.class,
        EvilometerZnachieniieSvoistva2Procedure.class,
        EvilometerZnachieniieSvoistva3Procedure.class,
        EvilometerZnachieniieSvoistva4Procedure.class,
        KillingVillagersProcedure.class,
        KillinganimalsProcedure.class,
        PositiveFertilizerProcedure.class,
        PositiveTamingProcedure.class,
        SpiritualGingerbreadPriZaviershieniiIspolzovaniiaProcedure.class
}, remap = false)
public abstract class PlayerVariablesAllocOptimizeMixin {
    @Redirect(
            method = "execute*",
            at = @At(
                    value = "NEW",
                    target = "()Lnet/mcreator/borninchaosv/network/BornInChaosV1ModVariables$PlayerVariables;"
            ),
            require = 0
    )
    private static BornInChaosV1ModVariables.PlayerVariables bic$reuseEmptyPlayerVariables() {
        if (!BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.enableGlobalFixes)) {
            return new BornInChaosV1ModVariables.PlayerVariables();
        }
        return BicPlayerVariablesHelper.EMPTY_READONLY;
    }
}
