package com.hp.borninchaosoptimizer.util;

import com.google.common.collect.ImmutableSet;
import com.hp.borninchaosoptimizer.config.OptimizerConfig;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.pathfinder.PathFinder;

/**
 * 针对生于混沌飞行生物（如 FOLLOW_RANGE 高达 150 的猩红追猎者）的三维飞行寻路优化实现：
 * 1. 保留原有的超远索敌距离（150 格），对超过 32 格的远距离追踪采用分段航点步进，避免单次分配 20x20=400 区块的 PathNavigationRegion；
 * 2. 拦截闲逛 AI 随机采样到地下实心岩层时的无效目标，消除三维 A* 遍历 2400 个立体节点造成的 100ms+ 服务端尖峰；
 * 3. 对连续寻路失败施加短暂退避冷却，避免不可达目标每刻反复拉满节点搜索预算。
 */
public class OptimizedFlyingPathNavigation extends FlyingPathNavigation {
    private static final int MAX_FLYING_NODES = 256;
    private static final double SEGMENT_STEP_DISTANCE = 28.0;
    private static final float MAX_REGION_RADIUS = 40.0f;

    private long lastFailedGameTime = -100L;
    @Nullable
    private BlockPos lastFailedTarget = null;

    public OptimizedFlyingPathNavigation(Mob mob, Level level) {
        super(mob, level);
    }

    @Override
    protected PathFinder createPathFinder(int maxVisitedNodes) {
        int capped = OptimizerConfig.isEnabled(OptimizerConfig.OPTIMIZE_FLYING_PATHFINDING)
                ? Math.min(maxVisitedNodes, MAX_FLYING_NODES)
                : maxVisitedNodes;
        return super.createPathFinder(capped);
    }

    @Nullable
    @Override
    protected Path createPath(Set<BlockPos> targets, int regionOffset, boolean offsetUpward, int accuracy, float followRange) {
        if (!OptimizerConfig.isEnabled(OptimizerConfig.OPTIMIZE_FLYING_PATHFINDING) || targets.isEmpty()) {
            return super.createPath(targets, regionOffset, offsetUpward, accuracy, followRange);
        }

        BlockPos mobPos = this.mob.blockPosition();
        if (targets.size() == 1) {
            BlockPos rawTarget = targets.iterator().next();
            long gameTime = this.level.getGameTime();

            // 若对同一不可达目标刚发生过寻路失败，则在 10 刻退避期内跳过重复搜索
            if (this.lastFailedTarget != null
                    && gameTime - this.lastFailedGameTime < 10L
                    && rawTarget.distManhattan(this.lastFailedTarget) <= 2) {
                return null;
            }

            // 无仇恨随机游荡时，若随机点落入地下实心方块内部，尝试向上寻找最多 3 格空气；仍被埋在地下则直接放弃本次随机点
            if (this.mob.getTarget() == null) {
                BlockPos adjusted = adjustStrollTargetOutOfSolid(rawTarget);
                if (adjusted == null) {
                    this.lastFailedGameTime = gameTime;
                    this.lastFailedTarget = rawTarget;
                    return null;
                }
                rawTarget = adjusted;
            }

            double dx = rawTarget.getX() - mobPos.getX();
            double dy = rawTarget.getY() - mobPos.getY();
            double dz = rawTarget.getZ() - mobPos.getZ();
            double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);

            BlockPos effectiveTarget = rawTarget;
            if (dist > 32.0) {
                double scale = SEGMENT_STEP_DISTANCE / dist;
                int wx = mobPos.getX() + Mth.floor(dx * scale);
                int wy = Mth.clamp(mobPos.getY() + Mth.floor(dy * scale), this.level.getMinBuildHeight() + 1, this.level.getMaxBuildHeight() - 2);
                int wz = mobPos.getZ() + Mth.floor(dz * scale);
                effectiveTarget = liftWaypointIfSolid(new BlockPos(wx, wy, wz));
                dist = SEGMENT_STEP_DISTANCE;
            }

            float boundedRange = Math.min(followRange, Math.max(20.0f, (float) dist + 8.0f));
            Path result = super.createPath(ImmutableSet.of(effectiveTarget), Math.min(regionOffset, 8), offsetUpward, accuracy, boundedRange);
            if (result == null) {
                this.lastFailedGameTime = gameTime;
                this.lastFailedTarget = rawTarget;
            }
            return result;
        }

        return super.createPath(targets, Math.min(regionOffset, 8), offsetUpward, accuracy, Math.min(followRange, MAX_REGION_RADIUS));
    }

    @Nullable
    private BlockPos adjustStrollTargetOutOfSolid(BlockPos pos) {
        if (!this.level.hasChunkAt(pos)) {
            return null;
        }
        for (int up = 0; up <= 3; up++) {
            BlockPos check = up == 0 ? pos : pos.above(up);
            if (this.level.getBlockState(check).getCollisionShape(this.level, check).isEmpty()) {
                return check;
            }
        }
        return null;
    }

    private BlockPos liftWaypointIfSolid(BlockPos pos) {
        if (!this.level.hasChunkAt(pos)) {
            return pos;
        }
        for (int up = 0; up <= 4; up++) {
            BlockPos check = up == 0 ? pos : pos.above(up);
            if (this.level.getBlockState(check).getCollisionShape(this.level, check).isEmpty()) {
                return check;
            }
        }
        return pos;
    }
}
