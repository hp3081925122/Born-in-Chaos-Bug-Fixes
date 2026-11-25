package com.hp.borninchaosoptimizer.mixin;

import com.hp.borninchaosoptimizer.config.OptimizerConfig;
import net.mcreator.borninchaosv.block.ClosedHoundTrapBlock;
import net.mcreator.borninchaosv.block.CobwebCoverBlock;
import net.mcreator.borninchaosv.block.HoundTrapBlock;
import net.mcreator.borninchaosv.block.SpinyShellTrapBlock;
import net.mcreator.borninchaosv.procedures.CobwebCoverPriObnovlieniiTikaProcedure;
import net.mcreator.borninchaosv.procedures.HoundTrapObnovlieniieTikaProcedure;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 消除捕兽夹、闭合捕兽夹、刺壳陷阱与蛛网覆层的永久每 1~2 刻空转自调度：
 * 改为仅在放置时检测一次，并在相邻/下方支撑方块发生变化（neighborChanged）时调度 1 刻支撑校验，
 * 保证破坏支撑方块后第 1 刻立即掉落，同时将世界中大量静置陷阱的常驻服务端开销降为 0。
 */
@Mixin(value = {
        HoundTrapBlock.class,
        ClosedHoundTrapBlock.class,
        SpinyShellTrapBlock.class,
        CobwebCoverBlock.class
})
public abstract class StaticTrapBlocksOptimizeMixin extends Block {
    protected StaticTrapBlocksOptimizeMixin(Properties properties) {
        super(properties);
    }

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void bic$checkSupportOnceWithoutInfiniteLoop(BlockState state, ServerLevel world, BlockPos pos, RandomSource random, CallbackInfo ci) {
        if (!OptimizerConfig.isEnabled(OptimizerConfig.OPTIMIZE_STATIC_TRAP_BLOCK_TICKS)) {
            return;
        }
        ci.cancel();
        super.tick(state, world, pos, random);
        if ((Object) this instanceof CobwebCoverBlock) {
            CobwebCoverPriObnovlieniiTikaProcedure.execute(world, pos.getX(), pos.getY(), pos.getZ());
        } else {
            HoundTrapObnovlieniieTikaProcedure.execute(world, pos.getX(), pos.getY(), pos.getZ());
        }
    }

    @Override
    protected void neighborChanged(BlockState state, Level world, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean movedByPiston) {
        super.neighborChanged(state, world, pos, neighborBlock, neighborPos, movedByPiston);
        if (OptimizerConfig.isEnabled(OptimizerConfig.OPTIMIZE_STATIC_TRAP_BLOCK_TICKS) && !world.isClientSide()) {
            if ((Object) this instanceof CobwebCoverBlock || neighborPos.equals(pos.below())) {
                world.scheduleTick(pos, this, 1);
            }
        }
    }
}
