package com.hp.borninchaosoptimizer.mixin.entity;

import com.hp.borninchaosoptimizer.ai.SafeAirborneStrollGoal;
import com.hp.borninchaosoptimizer.ai.SafeFlyingChargeGoal;
import com.hp.borninchaosoptimizer.ai.SegmentedFlyingPathNavigation;
import com.hp.borninchaosoptimizer.config.BicOptimizerConfig;
import net.mcreator.borninchaosv.entity.MrPumpkinControlledEntity;
import net.mcreator.borninchaosv.entity.MrPumpkinEntity;
import net.mcreator.borninchaosv.entity.RestlessSpiritEntity;
import net.mcreator.borninchaosv.entity.ScarletPersecutorEntity;
import net.mcreator.borninchaosv.entity.SenorPumpkinEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

/**
 * 为全部 5 种使用 FlyingPathNavigation 的三维飞行生物统一注入：
 * 1. 分段飞行寻路器（解决 ScarletPersecutor 150 格寻路抓取 529 区块与 2400 节点导致的 104.6ms 尖峰）；
 * 2. 安全版空中随机巡逻 Goal（解决原匿名 RandomStrollGoal 向地下实心岩层发起必败三维寻路）；
 * 3. 安全版空中冲刺 Goal（解决 RestlessSpirit 与 MrPumpkin 奇数 tick getTarget()==null 崩服 Bug）。
 */
@Mixin({
        ScarletPersecutorEntity.class,
        RestlessSpiritEntity.class,
        SenorPumpkinEntity.class,
        MrPumpkinEntity.class,
        MrPumpkinControlledEntity.class
})
public abstract class FlyingEntitiesNavMixin extends PathfinderMob {
    protected FlyingEntitiesNavMixin(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(method = "createNavigation", at = @At("HEAD"), cancellable = true)
    private void bic$useSegmentedFlyingNavigation(Level world, CallbackInfoReturnable<PathNavigation> cir) {
        if (BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.enableGlobalFixes)
                && BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.optimizeEntityPathfinding)) {
            cir.setReturnValue(new SegmentedFlyingPathNavigation(this, world));
        }
    }

    @Inject(method = "registerGoals", at = @At("TAIL"))
    private void bic$replaceBrokenAnonymousGoals(CallbackInfo ci) {
        boolean fixGoalNpe = BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.fixGoalTargetNpe);
        boolean optPath = BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.enableGlobalFixes)
                && BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.optimizeEntityPathfinding);
        if (!fixGoalNpe && !optPath) {
            return;
        }

        List<WrappedGoal> snapshot = new ArrayList<>(this.goalSelector.getAvailableGoals());
        Object self = this;
        for (WrappedGoal wrapped : snapshot) {
            Goal goal = wrapped.getGoal();
            int priority = wrapped.getPriority();
            if (!goal.getClass().isAnonymousClass()) {
                continue;
            }
            if (optPath && goal instanceof RandomStrollGoal) {
                this.goalSelector.removeGoal(goal);
                this.goalSelector.addGoal(priority, new SafeAirborneStrollGoal(this, 0.8, 20));
            } else if (fixGoalNpe && priority == 1 && !(goal instanceof RandomStrollGoal)) {
                if (self instanceof RestlessSpiritEntity) {
                    this.goalSelector.removeGoal(goal);
                    this.goalSelector.addGoal(1, new SafeFlyingChargeGoal(this, 2.5, 16.0));
                } else if (self instanceof MrPumpkinEntity) {
                    this.goalSelector.removeGoal(goal);
                    this.goalSelector.addGoal(1, new SafeFlyingChargeGoal(this, 2.0, 20.0));
                }
            }
        }
    }
}
