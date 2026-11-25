package com.hp.borninchaosoptimizer.mixin;

import com.hp.borninchaosoptimizer.config.OptimizerConfig;
import com.hp.borninchaosoptimizer.util.Batch5ProcedureHelper;
import net.mcreator.borninchaosv.procedures.DarkIcePriObnovlieniiTikaProcedure;
import net.minecraft.world.level.LevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 优化暗冰方块每刻融化倒计时过程：消除每刻无条件触发的 sendBlockUpdated 整块 NBT 同步包、客户端区块网格重绘标记与两次匿名内部类分配。
 */
@Mixin(value = DarkIcePriObnovlieniiTikaProcedure.class, remap = false)
public abstract class DarkIceTickOptimizeMixin {
    @Inject(method = "execute", at = @At("HEAD"), cancellable = true)
    private static void bic$optimizeDarkIceTick(LevelAccessor world, double x, double y, double z, CallbackInfo ci) {
        if (!OptimizerConfig.isEnabled(OptimizerConfig.OPTIMIZE_TEMPORARY_BLOCK_ENTITY_SYNC)) {
            return;
        }
        ci.cancel();
        Batch5ProcedureHelper.executeDarkIceTick(world, x, y, z);
    }
}
