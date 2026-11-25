package com.hp.borninchaosoptimizer.mixin.procedure;

import com.hp.borninchaosoptimizer.config.BicOptimizerConfig;
import net.mcreator.borninchaosv.procedures.SearedSpiritPriObnovlieniiTaktaSushchnostiProcedure;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 限制灼热之灵遇水扣血过程仅在服务端执行，避免客户端每 tick 构造伤害源空跑受击判定。
 */
@Mixin(value = SearedSpiritPriObnovlieniiTaktaSushchnostiProcedure.class, remap = false)
public abstract class SearedSpiritTickGuardMixin {
    @Inject(method = "execute", at = @At("HEAD"), cancellable = true)
    private static void bicOpt$serverOnly(LevelAccessor world, Entity entity, CallbackInfo ci) {
        if (BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.enableGlobalFixes)
                && BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.fixEntityTickClientSideMutations)
                && (entity == null || world.isClientSide())) {
            ci.cancel();
        }
    }
}
