package com.hp.borninchaosoptimizer.ai;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

/**
 * 为生于混沌三维飞行生物提供带可通行校验的空中随机巡逻选点，
 * 替代原模组在 ±16 格立方体内盲目选点（约半数落入地下实心岩石层，每 20 tick 触发必败满额三维 A* 寻路）的脆弱实现。
 */
public final class AirborneStrollHelper {
    private AirborneStrollHelper() {
    }

    @Nullable
    public static Vec3 findAirborneStrollPos(PathfinderMob mob) {
        Level level = mob.level();
        RandomSource random = mob.getRandom();
        double baseX = mob.getX();
        double baseY = mob.getY();
        double baseZ = mob.getZ();
        int minBuildY = level.getMinBuildHeight() + 1;
        int maxBuildY = level.getMaxBuildHeight() - 2;

        for (int attempt = 0; attempt < 6; attempt++) {
            double tx = baseX + (random.nextFloat() * 2.0f - 1.0f) * 14.0f;
            double ty = baseY + (random.nextFloat() * 2.0f - 1.0f) * 6.0f;
            double tz = baseZ + (random.nextFloat() * 2.0f - 1.0f) * 14.0f;
            if (ty < minBuildY || ty > maxBuildY) {
                continue;
            }
            BlockPos pos = BlockPos.containing(tx, ty, tz);
            if (!level.hasChunkAt(pos)) {
                continue;
            }
            if (level.isEmptyBlock(pos)) {
                return new Vec3(tx, ty, tz);
            }
        }
        return null;
    }
}
