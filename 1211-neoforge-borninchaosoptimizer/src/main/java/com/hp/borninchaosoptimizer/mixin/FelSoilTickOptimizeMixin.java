package com.hp.borninchaosoptimizer.mixin;

import com.hp.borninchaosoptimizer.config.OptimizerConfig;
import com.hp.borninchaosoptimizer.util.Batch5ProcedureHelper;
import net.mcreator.borninchaosv.procedures.FelSoilDestructibleObnovlieniieTikaProcedure;
import net.minecraft.world.level.LevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 优化可毁邪土方块每刻倒计时过程：消除每刻无条件触发的 sendBlockUpdated 整块 NBT 同步包与客户端区块重绘风暴。
 */
@Mixin(value = FelSoilDestructibleObnovlieniieTikaProcedure.class, remap = false)
public abstract class FelSoilTickOptimizeMixin {
    @Inject(method = "execute", at = @At("HEAD"), cancellable = true)
    private static void bic$optimizeFelSoilTick(LevelAccessor world, double x, double y, double z, CallbackInfo ci) {
        if (!OptimizerConfig.isEnabled(OptimizerConfig.OPTIMIZE_TEMPORARY_BLOCK_ENTITY_SYNC)) {
            return;
        }
        ci.cancel();
        Batch5ProcedureHelper.executeFelSoilDestructibleTick(world, x, y, z);
    }
}
