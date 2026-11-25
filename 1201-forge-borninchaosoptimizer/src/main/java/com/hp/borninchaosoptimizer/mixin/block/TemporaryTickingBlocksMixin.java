package com.hp.borninchaosoptimizer.mixin.block;

import com.hp.borninchaosoptimizer.config.BicOptimizerConfig;
import net.mcreator.borninchaosv.block.DarkIceBlock;
import net.mcreator.borninchaosv.block.FelSoilDestructibleBlock;
import net.mcreator.borninchaosv.block.PuddleofStimulationBlock;
import net.mcreator.borninchaosv.block.PuddleofintoxicationBlock;
import net.mcreator.borninchaosv.procedures.DarkIcePriDobavlieniiBlokaProcedure;
import net.mcreator.borninchaosv.procedures.DarkIcePriObnovlieniiTikaProcedure;
import net.mcreator.borninchaosv.procedures.FelSoilDestructibleObnovlieniieTikaProcedure;
import net.mcreator.borninchaosv.procedures.FelSoilDestructiblePriDobavlieniiBlokaProcedure;
import net.mcreator.borninchaosv.procedures.PuddleofStimulationticProcedure;
import net.mcreator.borninchaosv.procedures.PuddleofintoxicationObnovlieniieTikaProcedure;
import net.mcreator.borninchaosv.procedures.PuddleofintoxicationPriDobavlieniiBlokaProcedure;
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
 * 优化暗冰、可破坏邪土、亢奋水洼与迷醉水洼的方块计划刻调度：
 * 将原本每 1 tick 调度合并为可配置步长（默认 5 tick，总持续时间保持不变），
 * 并在方块融化/消散后立即停止尾随空调度。
 */
@Mixin({
        DarkIceBlock.class,
        FelSoilDestructibleBlock.class,
        PuddleofStimulationBlock.class,
        PuddleofintoxicationBlock.class
})
public abstract class TemporaryTickingBlocksMixin extends Block {
    private TemporaryTickingBlocksMixin(Properties properties) {
        super(properties);
    }

    @Inject(method = "onPlace", at = @At("HEAD"), cancellable = true)
    private void bicOpt$onPlaceScheduled(BlockState blockstate, Level world, BlockPos pos, BlockState oldState, boolean moving, CallbackInfo ci) {
        if (!BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.enableGlobalFixes)
                || !BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.optimizeTemporaryTickingBlocks)) {
            return;
        }
        ci.cancel();
        super.onPlace(blockstate, world, pos, oldState, moving);
        int interval = Math.max(1, BicOptimizerConfig.getInt(BicOptimizerConfig.COMMON.temporaryBlockTickInterval));
        world.scheduleTick(pos, this, interval);

        Block self = this;
        if (self instanceof DarkIceBlock) {
            DarkIcePriDobavlieniiBlokaProcedure.execute(world, pos.getX(), pos.getY(), pos.getZ());
        } else if (self instanceof FelSoilDestructibleBlock) {
            FelSoilDestructiblePriDobavlieniiBlokaProcedure.execute(world, pos.getX(), pos.getY(), pos.getZ());
        } else if (self instanceof PuddleofStimulationBlock || self instanceof PuddleofintoxicationBlock) {
            PuddleofintoxicationPriDobavlieniiBlokaProcedure.execute(world, pos.getX(), pos.getY(), pos.getZ());
        }
    }

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void bicOpt$onTickScheduled(BlockState blockstate, ServerLevel world, BlockPos pos, RandomSource random, CallbackInfo ci) {
        if (!BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.enableGlobalFixes)
                || !BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.optimizeTemporaryTickingBlocks)) {
            return;
        }
        ci.cancel();
        super.tick(blockstate, world, pos, random);

        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();
        Block self = this;
        if (self instanceof DarkIceBlock) {
            DarkIcePriObnovlieniiTikaProcedure.execute(world, x, y, z);
        } else if (self instanceof FelSoilDestructibleBlock) {
            FelSoilDestructibleObnovlieniieTikaProcedure.execute(world, x, y, z);
        } else if (self instanceof PuddleofStimulationBlock) {
            PuddleofStimulationticProcedure.execute(world, x, y, z);
        } else if (self instanceof PuddleofintoxicationBlock) {
            PuddleofintoxicationObnovlieniieTikaProcedure.execute(world, x, y, z);
        }

        if (world.getBlockState(pos).is(this)) {
            int interval = Math.max(1, BicOptimizerConfig.getInt(BicOptimizerConfig.COMMON.temporaryBlockTickInterval));
            world.scheduleTick(pos, this, interval);
        }
    }
}
