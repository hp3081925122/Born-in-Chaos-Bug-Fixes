package com.hp.borninchaosoptimizer.mixin.block;

import com.hp.borninchaosoptimizer.config.BicOptimizerConfig;
import net.mcreator.borninchaosv.procedures.FelSoilDestructibleObnovlieniieTikaProcedure;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 优化可破坏邪土倒计时销毁过程：移除每 tick sendBlockUpdated 光照区块重编译风暴，并将 == 0.0 修复为 <= 0.0。
 */
@Mixin(value = FelSoilDestructibleObnovlieniieTikaProcedure.class, remap = false)
public abstract class FelSoilTickProcedureMixin {
    @Inject(method = "execute", at = @At("HEAD"), cancellable = true)
    private static void bicOpt$tickFelSoil(LevelAccessor world, double x, double y, double z, CallbackInfo ci) {
        if (!BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.enableGlobalFixes)
                || !BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.optimizeTemporaryTickingBlocks)) {
            return;
        }
        ci.cancel();
        if (world.isClientSide()) {
            return;
        }
        BlockPos pos = BlockPos.containing(x, y, z);
        BlockEntity be = world.getBlockEntity(pos);
        if (be == null) {
            return;
        }
        int step = Math.max(1, BicOptimizerConfig.getInt(BicOptimizerConfig.COMMON.temporaryBlockTickInterval));
        double next = be.getPersistentData().getDouble("destructible") - step;
        if (next <= 0.0) {
            world.destroyBlock(pos, false);
        } else {
            be.getPersistentData().putDouble("destructible", next);
        }
    }
}
