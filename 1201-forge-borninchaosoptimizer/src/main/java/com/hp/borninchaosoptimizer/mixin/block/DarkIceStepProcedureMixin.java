package com.hp.borninchaosoptimizer.mixin.block;

import com.hp.borninchaosoptimizer.config.BicOptimizerConfig;
import net.mcreator.borninchaosv.entity.KrampusEntity;
import net.mcreator.borninchaosv.entity.LifestealerEntity;
import net.mcreator.borninchaosv.entity.LifestealerTrueFormEntity;
import net.mcreator.borninchaosv.entity.NightmareStalkerEntity;
import net.mcreator.borninchaosv.init.BornInChaosV1ModBlocks;
import net.mcreator.borninchaosv.procedures.DarkIceKoghdaSushchnostKhoditPoBlokuProcedure;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 修复生物踩踏暗冰蔓延过程：
 * 1. 仅服务端权威执行，消除客户端放置周围 8 格幽灵暗冰；
 * 2. 移除每步触发的 sendBlockUpdated 网络包，并将每格 5 次重复 getBlockState 合并为 1 次。
 */
@Mixin(value = DarkIceKoghdaSushchnostKhoditPoBlokuProcedure.class, remap = false)
public abstract class DarkIceStepProcedureMixin {
    @Inject(method = "execute", at = @At("HEAD"), cancellable = true)
    private static void bicOpt$safeStepSpread(LevelAccessor world, double x, double y, double z, Entity entity, CallbackInfo ci) {
        if (!BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.enableGlobalFixes)
                || !BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.optimizeTemporaryTickingBlocks)) {
            return;
        }
        ci.cancel();
        if (entity == null || world.isClientSide()) {
            return;
        }
        if (!(entity instanceof NightmareStalkerEntity
                || entity instanceof LifestealerEntity
                || entity instanceof LifestealerTrueFormEntity
                || entity instanceof KrampusEntity)) {
            return;
        }

        BlockPos center = BlockPos.containing(x, y, z);
        BlockEntity be = world.getBlockEntity(center);
        if (be != null) {
            be.getPersistentData().putDouble("splitting", be.getPersistentData().getDouble("splitting") + 2.0);
        }

        BlockState darkIceState = BornInChaosV1ModBlocks.DARK_ICE.get().defaultBlockState();
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                if (dx == 0 && dz == 0) {
                    continue;
                }
                BlockPos targetPos = BlockPos.containing(x + dx, y, z + dz);
                Block targetBlock = world.getBlockState(targetPos).getBlock();
                if (targetBlock == Blocks.WATER || targetBlock == Blocks.LAVA
                        || targetBlock == Blocks.KELP_PLANT || targetBlock == Blocks.SEAGRASS
                        || targetBlock == Blocks.TALL_SEAGRASS) {
                    world.setBlock(targetPos, darkIceState, 3);
                }
            }
        }
    }
}
