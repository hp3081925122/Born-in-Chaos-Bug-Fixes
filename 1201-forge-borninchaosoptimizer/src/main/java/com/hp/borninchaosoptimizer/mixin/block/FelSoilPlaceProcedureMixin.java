package com.hp.borninchaosoptimizer.mixin.block;

import com.hp.borninchaosoptimizer.config.BicOptimizerConfig;
import net.mcreator.borninchaosv.procedures.FelSoilDestructiblePriDobavlieniiBlokaProcedure;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 优化可破坏邪土放置初始化过程：直接设置服务端倒计时，消除无意义的 sendBlockUpdated 区块网格标脏。
 */
@Mixin(value = FelSoilDestructiblePriDobavlieniiBlokaProcedure.class, remap = false)
public abstract class FelSoilPlaceProcedureMixin {
    @Inject(method = "execute", at = @At("HEAD"), cancellable = true)
    private static void bicOpt$initWithoutPacket(LevelAccessor world, double x, double y, double z, CallbackInfo ci) {
        if (!BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.enableGlobalFixes)
                || !BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.optimizeTemporaryTickingBlocks)) {
            return;
        }
        ci.cancel();
        if (!world.isClientSide()) {
            BlockEntity be = world.getBlockEntity(BlockPos.containing(x, y, z));
            if (be != null) {
                be.getPersistentData().putDouble("destructible", 200.0);
            }
        }
    }
}
