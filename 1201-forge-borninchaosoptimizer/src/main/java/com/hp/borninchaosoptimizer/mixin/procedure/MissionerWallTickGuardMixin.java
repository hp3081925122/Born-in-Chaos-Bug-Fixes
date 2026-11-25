package com.hp.borninchaosoptimizer.mixin.procedure;

import com.hp.borninchaosoptimizer.config.BicOptimizerConfig;
import net.mcreator.borninchaosv.procedures.MissionerPriObnovlieniiTikaSushchnostiProcedure;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 限制传教士卡墙脱困瞬移仅在服务端执行，防止客户端自行修改坐标导致拉回抖动。
 */
@Mixin(value = MissionerPriObnovlieniiTikaSushchnostiProcedure.class, remap = false)
public abstract class MissionerWallTickGuardMixin {
    @Inject(method = "execute", at = @At("HEAD"), cancellable = true)
    private static void bicOpt$serverOnly(double x, double y, double z, Entity entity, CallbackInfo ci) {
        if (BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.enableGlobalFixes)
                && BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.fixEntityTickClientSideMutations)
                && (entity == null || entity.level().isClientSide())) {
            ci.cancel();
        }
    }
}
