package com.hp.borninchaosoptimizer.mixin.procedure;

import com.hp.borninchaosoptimizer.config.BicOptimizerConfig;
import net.mcreator.borninchaosv.procedures.MaggotPriObnovlieniiTikaSushchnostiProcedure;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 限制腐蛆出场动画状态每 tick 过程仅在服务端执行，避免客户端竞争修改 PersistentData。
 */
@Mixin(value = MaggotPriObnovlieniiTikaSushchnostiProcedure.class, remap = false)
public abstract class MaggotTickGuardMixin {
    @Inject(method = "execute", at = @At("HEAD"), cancellable = true)
    private static void bicOpt$serverOnly(Entity entity, CallbackInfo ci) {
        if (BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.enableGlobalFixes)
                && BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.fixEntityTickClientSideMutations)
                && (entity == null || entity.level().isClientSide())) {
            ci.cancel();
        }
    }
}
