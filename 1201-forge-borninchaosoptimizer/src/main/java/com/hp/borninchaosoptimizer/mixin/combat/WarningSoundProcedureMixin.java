package com.hp.borninchaosoptimizer.mixin.combat;

import com.hp.borninchaosoptimizer.config.BicOptimizerConfig;
import net.mcreator.borninchaosv.procedures.WarningSoundProcedure;
import net.minecraftforge.event.TickEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 优化 WarningSoundProcedure：客户端完全无需每 tick 执行（内部仅在服务端添加药水效果）。
 */
@Mixin(value = WarningSoundProcedure.class, remap = false)
public abstract class WarningSoundProcedureMixin {
    @Inject(method = "onPlayerTick", at = @At("HEAD"), cancellable = true)
    private static void bic$skipClientPlayerTick(TickEvent.PlayerTickEvent event, CallbackInfo ci) {
        if (!BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.enableGlobalFixes)) {
            return;
        }
        if (event == null || event.phase != TickEvent.Phase.END || event.player == null || event.player.level().isClientSide()) {
            ci.cancel();
        }
    }
}
