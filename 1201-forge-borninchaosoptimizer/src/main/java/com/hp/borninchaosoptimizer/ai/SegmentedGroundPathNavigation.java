package com.hp.borninchaosoptimizer.ai;

import com.hp.borninchaosoptimizer.config.BicOptimizerConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.pathfinder.PathFinder;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Set;

/**
 * 分段式地面寻路器：
 * 用于 NightmareStalker 等超远 FOLLOW_RANGE（80 格）地面生物，
 * 保留 80 格索敌锁定距离，将远距离地面导航拆分为可配置步长（默认 32 格）的局部寻路并施加不可达冷却。
 */
public final class SegmentedGroundPathNavigation extends GroundPathNavigation {
    private BlockPos lastUnreachableTarget;
    private long unreachableCooldownUntil;

    public SegmentedGroundPathNavigation(Mob mob, Level level) {
        super(mob, level);
    }

    @Override
    protected PathFinder createPathFinder(int maxVisitedNodes) {
        int budget = BicOptimizerConfig.getInt(BicOptimizerConfig.COMMON.maxGroundVisitedNodes);
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
            double dz = target.getZ() - mobPos.getZ();
            double horizSqr = dx * dx + dz * dz;
            if (horizSqr > maxSegmentDistSqr) {
                if (effectiveTargets == targets) {
                    effectiveTargets = new HashSet<>(targets.size());
                }
                double invScale = maxSegmentDist / Math.sqrt(horizSqr);
                int rx = (int) Math.floor(mobPos.getX() + dx * invScale);
                int rz = (int) Math.floor(mobPos.getZ() + dz * invScale);
                BlockPos checkChunkPos = new BlockPos(rx, mobPos.getY(), rz);
                if (this.level.hasChunkAt(checkChunkPos)) {
                    int ry = this.level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, rx, rz);
                    if (Math.abs(ry - mobPos.getY()) > 16) {
                        ry = mobPos.getY();
                    }
                    effectiveTargets.add(new BlockPos(rx, ry, rz));
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
