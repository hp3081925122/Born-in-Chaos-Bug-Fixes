package com.hp.borninchaosoptimizer.ai;

import com.hp.borninchaosoptimizer.config.BicOptimizerConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.pathfinder.PathFinder;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Set;

/**
 * 分段式三维飞行寻路器：
 * 保留生物原有的超远索敌距离（如 ScarletPersecutor 的 150 格），
 * 但将单次 PathNavigationRegion 区块抓取范围与三维体素 A* 节点上限约束在可配置分段预算内；
 * 当目标不可达时增加短暂重算退避冷却，彻底消除 30 只飞行生物并发触发 2400 节点三维搜索造成的 100ms+ 尖峰。
 */
public final class SegmentedFlyingPathNavigation extends FlyingPathNavigation {
    private BlockPos lastUnreachableTarget;
    private long unreachableCooldownUntil;

    public SegmentedFlyingPathNavigation(Mob mob, Level level) {
        super(mob, level);
    }

    @Override
    protected PathFinder createPathFinder(int maxVisitedNodes) {
        int budget = BicOptimizerConfig.getInt(BicOptimizerConfig.COMMON.maxFlyingVisitedNodes);
        return super.createPathFinder(Math.min(maxVisitedNodes, budget));
    }

    @Nullable
    @Override
    protected Path createPath(Set<BlockPos> targets, int regionOffset, boolean offsetUpward, int accuracy, float followRange) {
        if (targets.isEmpty() || !BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.enableGlobalFixes)
                || !BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.optimizeEntityPathfinding)) {
            return super.createPath(targets, regionOffset, offsetUpward, accuracy, followRange);
        }

        long now = this.level.getGameTime();
        if (this.lastUnreachableTarget != null && now < this.unreachableCooldownUntil && targets.size() == 1) {
            BlockPos single = targets.iterator().next();
            if (single.distManhattan(this.lastUnreachableTarget) <= 3 && this.path != null && !this.path.isDone()) {
                return this.path;
            }
        }

        float maxSegment = (float) BicOptimizerConfig.getInt(BicOptimizerConfig.COMMON.maxPathSegmentRange);
        float effectiveRange = Math.min(followRange, maxSegment);
        double maxSegmentDist = Math.max(12.0, maxSegment - 4.0);
        double maxSegmentDistSqr = maxSegmentDist * maxSegmentDist;

        BlockPos mobPos = this.mob.blockPosition();
        Set<BlockPos> effectiveTargets = targets;
        for (BlockPos target : targets) {
            double dx = target.getX() - mobPos.getX();
            double dy = target.getY() - mobPos.getY();
            double dz = target.getZ() - mobPos.getZ();
            double distSqr = dx * dx + dy * dy + dz * dz;
            if (distSqr > maxSegmentDistSqr) {
                if (effectiveTargets == targets) {
                    effectiveTargets = new HashSet<>(targets.size());
                }
                double invScale = maxSegmentDist / Math.sqrt(distSqr);
                BlockPos relay = BlockPos.containing(
                        mobPos.getX() + dx * invScale,
                        mobPos.getY() + dy * invScale,
                        mobPos.getZ() + dz * invScale
                );
                if (this.level.hasChunkAt(relay)) {
                    effectiveTargets.add(relay);
                }
            } else if (effectiveTargets != targets) {
                effectiveTargets.add(target);
            }
        }

        if (effectiveTargets.isEmpty()) {
            return null;
        }

        Path result = super.createPath(effectiveTargets, Math.min(regionOffset, 12), offsetUpward, accuracy, effectiveRange);
        if (effectiveTargets.size() == 1) {
            BlockPos queried = effectiveTargets.iterator().next();
            if (result == null || !result.canReach()) {
                this.lastUnreachableTarget = queried;
                this.unreachableCooldownUntil = now + 16L;
            } else {
                this.lastUnreachableTarget = null;
            }
        }
        return result;
    }
}
