package com.hp.borninchaosoptimizer.mixin.combat;

import com.hp.borninchaosoptimizer.config.BicOptimizerConfig;
import com.hp.borninchaosoptimizer.util.BicCrossCheckHelper;
import net.mcreator.borninchaosv.procedures.NutHammerPriUdariePoSushchnostiInstrumientomProcedure;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 修复坚果锤击落头盔过程的附魔头盔复制漏洞、生物头盔复制漏洞、无头盔生成空掉落物实体及冗余背包深拷贝问题。
 */
@Mixin(value = NutHammerPriUdariePoSushchnostiInstrumientomProcedure.class, remap = false)
public abstract class NutHammerDisarmProcedureMixin {
    @Inject(method = "execute", at = @At("HEAD"), cancellable = true)
    private static void bic$fixNutHammerDisarm(LevelAccessor world, Entity entity, CallbackInfo ci) {
        if (!BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.enableGlobalFixes)
                || !BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.fixNutHammerDisarm)) {
            return;
        }
        ci.cancel();
        BicCrossCheckHelper.executeNutHammerDisarm(world, entity);
    }
}
