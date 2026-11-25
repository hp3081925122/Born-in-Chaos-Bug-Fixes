package com.hp.borninchaosoptimizer.util;

import com.google.common.collect.ImmutableSet;
import com.hp.borninchaosoptimizer.config.OptimizerConfig;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.pathfinder.PathFinder;

/**
 * 针对超远索敌地面生物（如 FOLLOW_RANGE 高达 80 的噩梦潜猎者）的寻路优化实现：
 * 保留 80 格完整索敌仇恨范围，对近距离游荡收缩 PathNavigationRegion 区块缓存半径，对超远目标采用分段地面航点推进。
 */
public class OptimizedGroundPathNavigation extends GroundPathNavigation {
    private static final int MAX_GROUND_NODES = 512;
    private static final double SEGMENT_STEP_DISTANCE = 34.0;
    private static final float MAX_REGION_RADIUS = 44.0f;

    public OptimizedGroundPathNavigation(Mob mob, Level level) {
        super(mob, level);
    }

    @Override
    protected PathFinder createPathFinder(int maxVisitedNodes) {
        int capped = OptimizerConfig.isEnabled(OptimizerConfig.OPTIMIZE_FLYING_PATHFINDING)
                ? Math.min(maxVisitedNodes, MAX_GROUND_NODES)
                : maxVisitedNodes;
        return super.createPathFinder(capped);
    }

    @Nullable
    @Override
    protected Path createPath(Set<BlockPos> targets, int regionOffset, boolean offsetUpward, int accuracy, float followRange) {
        if (!OptimizerConfig.isEnabled(OptimizerConfig.OPTIMIZE_FLYING_PATHFINDING) || targets.isEmpty()) {
            return super.createPath(targets, regionOffset, offsetUpward, accuracy, followRange);
        }

        if (targets.size() == 1) {
            BlockPos mobPos = this.mob.blockPosition();
            BlockPos rawTarget = targets.iterator().next();
            double dx = rawTarget.getX() - mobPos.getX();
            double dy = rawTarget.getY() - mobPos.getY();
            double dz = rawTarget.getZ() - mobPos.getZ();
            double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);

            BlockPos effectiveTarget = rawTarget;
            if (dist > 38.0) {
                double scale = SEGMENT_STEP_DISTANCE / dist;
                int wx = mobPos.getX() + Mth.floor(dx * scale);
                int wz = mobPos.getZ() + Mth.floor(dz * scale);
                int wy = Mth.floor(mobPos.getY() + dy * scale);
                BlockPos candidate = new BlockPos(wx, wy, wz);
                if (this.level.hasChunkAt(candidate)) {
                    int surfaceY = this.level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, wx, wz);
                    if (Math.abs(surfaceY - wy) <= 12) {
                        candidate = new BlockPos(wx, surfaceY, wz);
                    }
                }
                effectiveTarget = candidate;
                dist = SEGMENT_STEP_DISTANCE;
            }

            float boundedRange = Math.min(followRange, Math.max(24.0f, (float) dist + 8.0f));
            return super.createPath(ImmutableSet.of(effectiveTarget), Math.min(regionOffset, 8), offsetUpward, accuracy, boundedRange);
        }

        return super.createPath(targets, Math.min(regionOffset, 8), offsetUpward, accuracy, Math.min(followRange, MAX_REGION_RADIUS));
    }
}
