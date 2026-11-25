package com.hp.borninchaosoptimizer.mixin;

import com.hp.borninchaosoptimizer.config.OptimizerConfig;
import com.hp.borninchaosoptimizer.util.Batch5ProcedureHelper;
import net.mcreator.borninchaosv.procedures.NutHammerPriUdariePoSushchnostiInstrumientomProcedure;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 修复坚果锤击落头盔过程的三大漏洞与性能问题：
 * 1. 攻击未戴头盔的目标仍生成空 ItemStack 掉落物与眩晕特效；
 * 2. 攻击玩家时使用按物品类型匹配的 clearOrCountMatchingItems，导致优先删除背包里未附魔的同类头盔并复制头上附魔头盔；
 * 3. 攻击非玩家生物时遍历全部 ItemHandler 槽位做无用深拷贝，且对无 ItemHandler 能力的模组生物无限复制头盔。
 */
@Mixin(value = NutHammerPriUdariePoSushchnostiInstrumientomProcedure.class, remap = false)
public abstract class NutHammerDisarmFixMixin {
    @Inject(method = "execute", at = @At("HEAD"), cancellable = true)
    private static void bic$fixNutHammerDisarm(LevelAccessor world, Entity entity, CallbackInfo ci) {
        if (!OptimizerConfig.isEnabled(OptimizerConfig.FIX_NUT_HAMMER_DISARM)) {
            return;
        }
        ci.cancel();
        Batch5ProcedureHelper.executeNutHammerDisarm(world, entity);
    }
}
