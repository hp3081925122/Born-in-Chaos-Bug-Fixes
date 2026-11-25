package com.hp.borninchaosoptimizer.mixin.block;

import com.hp.borninchaosoptimizer.config.BicOptimizerConfig;
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
 * 消除猎犬夹、闭合猎犬夹、棘壳陷阱与蛛网覆层的永久每 1~2 tick 空转自调度：
 * 原模组仅为了检测下方支撑方块是否被破坏而让所有陷阱方块永久每 tick 自调度；
 * 现改为放置时检测一次，并在下方/相邻方块发生形状更新（updateShape）或邻居更新（neighborChanged）时
 * 立即调度 1 tick 检测，既保持破坏支撑方块后第 1 tick 瞬时掉落，又将静置陷阱的常驻 TPS 开销降为 0。
 */
@Mixin({
        HoundTrapBlock.class,
        ClosedHoundTrapBlock.class,
        SpinyShellTrapBlock.class,
        CobwebCoverBlock.class
})
public abstract class StaticTrapBlocksMixin extends Block {
    private StaticTrapBlocksMixin(Properties properties) {
        super(properties);
    }

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void bicOpt$singleSupportCheckTick(BlockState blockstate, ServerLevel world, BlockPos pos, RandomSource random, CallbackInfo ci) {
        if (!BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.enableGlobalFixes)
                || !BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.optimizeStaticTrapBlockTicks)) {
            return;
        }
        ci.cancel();
        super.tick(blockstate, world, pos, random);
        if ((Block) this instanceof CobwebCoverBlock) {
            CobwebCoverPriObnovlieniiTikaProcedure.execute(world, pos.getX(), pos.getY(), pos.getZ());
        } else {
            HoundTrapObnovlieniieTikaProcedure.execute(world, pos.getX(), pos.getY(), pos.getZ());
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public void neighborChanged(BlockState state, Level world, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean moving) {
        super.neighborChanged(state, world, pos, neighborBlock, neighborPos, moving);
        if (BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.enableGlobalFixes)
                && BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.optimizeStaticTrapBlockTicks)
                && !world.isClientSide()) {
            if ((Block) this instanceof CobwebCoverBlock || neighborPos.equals(pos.below())) {
                world.scheduleTick(pos, this, 1);
            }
        }
    }
}
