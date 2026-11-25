package com.hp.borninchaosoptimizer.mixin;

import com.hp.borninchaosoptimizer.config.OptimizerConfig;
import com.hp.borninchaosoptimizer.util.OptimizedGroundPathNavigation;
import net.mcreator.borninchaosv.entity.NightmareStalkerEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;

/**
 * 为 80 格超远索敌的噩梦潜猎者替换分段优化地面导航器，在完全保留 80 格追击能力的前提下降低寻路区块缓存与节点开销。
 */
@Mixin(NightmareStalkerEntity.class)
public abstract class NightmareStalkerNavigationMixin extends Monster {
    protected NightmareStalkerNavigationMixin(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        if (OptimizerConfig.isEnabled(OptimizerConfig.OPTIMIZE_FLYING_PATHFINDING)) {
            return new OptimizedGroundPathNavigation(this, level);
        }
        return super.createNavigation(level);
    }
}
