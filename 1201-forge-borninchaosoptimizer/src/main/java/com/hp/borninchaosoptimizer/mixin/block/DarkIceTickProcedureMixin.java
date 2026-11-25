package com.hp.borninchaosoptimizer.mixin.block;

import com.hp.borninchaosoptimizer.config.BicOptimizerConfig;
import net.mcreator.borninchaosv.procedures.DarkIcePriObnovlieniiTikaProcedure;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 优化暗冰倒计时融化过程：
 * 1. 彻底移除每 tick 对所有客户端广播的 sendBlockUpdated 网络包与区块重编译风暴；
 * 2. 将 == 0.0 修复为 <= 0.0，杜绝未初始化或指令放置暗冰变为负数后永不融化的死循环泄漏。
 */
@Mixin(value = DarkIcePriObnovlieniiTikaProcedure.class, remap = false)
public abstract class DarkIceTickProcedureMixin {
    @Inject(method = "execute", at = @At("HEAD"), cancellable = true)
    private static void bicOpt$tickDarkIce(LevelAccessor world, double x, double y, double z, CallbackInfo ci) {
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
        double next = be.getPersistentData().getDouble("splitting") - step;
        if (next <= 0.0) {
            world.destroyBlock(pos, false);
            world.setBlock(pos, Blocks.WATER.defaultBlockState(), 3);
        } else {
            be.getPersistentData().putDouble("splitting", next);
        }
    }
}
