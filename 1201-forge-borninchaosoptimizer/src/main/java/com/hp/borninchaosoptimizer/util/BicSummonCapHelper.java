package com.hp.borninchaosoptimizer.util;

import com.hp.borninchaosoptimizer.config.BicOptimizerConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.List;

/**
 * 为生于混沌高频召唤/分裂/衍生刷怪过程提供局部同类实体密度保护上限，
 * 防止虫群宿主、母蜘蛛、唤骨者、灵魂向导与亡灵召唤等绕过原版 70 只刷怪上限导致局部堆怪卡服。
 */
public final class BicSummonCapHelper {
    private BicSummonCapHelper() {
    }

    /**
     * 带局部同类密度上限的安全生成：达到阈值后返回 null（原模组调用方均已判空），不破坏音效、冷却与正常战斗体验。
     */
    public static <T extends Entity> T spawnWithCap(
            EntityType<T> entityType, ServerLevel level, BlockPos pos, MobSpawnType spawnType
    ) {
        if (!BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.enableGlobalFixes)
                || !BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.capMinionAndSwarmSpawning)) {
            return entityType.spawn(level, pos, spawnType);
        }
        int maxNearby = BicOptimizerConfig.getInt(BicOptimizerConfig.COMMON.maxNearbySummonedMinions);
        int radius = BicOptimizerConfig.getInt(BicOptimizerConfig.COMMON.minionCapRadius);
        AABB box = new AABB(pos).inflate(radius);
        List<T> nearby = new ArrayList<>(Math.min(maxNearby, 32));
        level.getEntities(entityType, box, Entity::isAlive, nearby, maxNearby);
        if (nearby.size() >= maxNearby) {
            return null;
        }
        return entityType.spawn(level, pos, spawnType);
    }
}
