package com.hp.borninchaosoptimizer.mixin;

import com.hp.borninchaosoptimizer.config.OptimizerConfig;
import net.mcreator.borninchaosv.procedures.SquireoftheDarkLordPProcedure;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 优化黑暗领主侍从进度每刻检查过程：
 * 1. 彻底拦截客户端每刻无意义执行的 7 次全背包 ItemStack 遍历（客户端本身无法发放进度）；
 * 2. 服务端按 20 刻（1 秒）节流检查，消除携带任意暗金属武器时每刻扫描全背包的开销。
 */
@Mixin(value = SquireoftheDarkLordPProcedure.class, remap = false)
public abstract class SquireDarkLordTickOptimizeMixin {
    @Inject(method = "execute", at = @At("HEAD"), cancellable = true)
    private static void bic$skipClientAndThrottleAdvancementScan(Entity entity, CallbackInfo ci) {
        if (!OptimizerConfig.isEnabled(OptimizerConfig.OPTIMIZE_EQUIPMENT_INVENTORY_TICKS)) {
            return;
        }
        if (!(entity instanceof ServerPlayer) || entity.tickCount % 20 != 0) {
            ci.cancel();
        }
    }
}
