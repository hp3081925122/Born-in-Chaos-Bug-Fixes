package com.hp.borninchaosoptimizer.mixin.procedure;

import com.hp.borninchaosoptimizer.config.BicOptimizerConfig;
import net.mcreator.borninchaosv.procedures.SquireoftheDarkLordPProcedure;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 优化暗金属武器集齐进度检查过程（SquireoftheDarkLordPProcedure）：
 * 拦截客户端每 tick 恒真执行的最多 7x41 格全背包扫描，并在服务端按 20 tick 周期节流。
 */
@Mixin(value = SquireoftheDarkLordPProcedure.class, remap = false)
public abstract class SquireDarkLordTickProcedureMixin {
    @Inject(method = "execute", at = @At("HEAD"), cancellable = true)
    private static void bic$throttleSquireInventoryScan(Entity entity, CallbackInfo ci) {
        if (!BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.enableGlobalFixes)
                || !BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.optimizeInventoryAndArmorTicks)) {
            return;
        }
        if (!(entity instanceof ServerPlayer player) || ((player.tickCount + player.getId()) % 20 != 0)) {
            ci.cancel();
        }
    }
}
