package com.hp.borninchaosoptimizer.mixin.entity;

import com.hp.borninchaosoptimizer.config.BicOptimizerConfig;
import net.mcreator.borninchaosv.entity.*;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;

/**
 * 统一优化生于混沌全部 13 个内部 RangedAttackGoal 类：
 * 1. 增加奇数 tick 目标判空与存活校验，防止目标被提前移除后触发空指针；
 * 2. 对标原版 MeleeAttackGoal 为追击分支的 navigation.moveTo 增加按距离分级的动态重算冷却（4~20 tick），
 *    消除玩家移动或目标不可达时每 tick 重复重跑完整 A* 寻路的开销。
 */
@Mixin({
        BonescallerEntity.RangedAttackGoal.class,
        BonescallerNotDespawnEntity.RangedAttackGoal.class,
        DarkVortexEntity.RangedAttackGoal.class,
        LordPumpkinheadHeadEntity.RangedAttackGoal.class,
        LordPumpkinheadWithoutaHorseEntity.RangedAttackGoal.class,
        MissionaryRaiderEntity.RangedAttackGoal.class,
        MissionerEntity.RangedAttackGoal.class,
        MrsPumpkinEntity.RangedAttackGoal.class,
        PumpkinheadEntity.RangedAttackGoal.class,
        SirPumpkinheadWithoutHorseEntity.RangedAttackGoal.class,
        SupremeBonescallerEntity.RangedAttackGoal.class,
        SupremeBonescallerNotDespawnEntity.RangedAttackGoal.class,
        SupremeBonescallerStage2Entity.RangedAttackGoal.class
})
public abstract class CustomRangedGoalsMixin {
    @Shadow(remap = false)
    @Final
    private Mob mob;

    @Shadow(remap = false)
    @Nullable
    private LivingEntity target;

    @Unique
    private int bic$repathCooldown;

    @Inject(method = "stop", at = @At("TAIL"))
    private void bic$resetCooldownOnStop(CallbackInfo ci) {
        this.bic$repathCooldown = 0;
    }

    @Inject(method = "canContinueToUse", at = @At("HEAD"), cancellable = true)
    private void bic$guardNullTargetInContinue(CallbackInfoReturnable<Boolean> cir) {
        if (BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.enableGlobalFixes)
                && BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.optimizeRangedAttackGoals)
                && this.target == null
                && this.mob.getTarget() == null) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void bic$guardNullTargetInTick(CallbackInfo ci) {
        if (BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.enableGlobalFixes)
                && BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.optimizeRangedAttackGoals)
                && (this.target == null || !this.target.isAlive())) {
            ci.cancel();
        }
    }

    @Redirect(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/ai/navigation/PathNavigation;moveTo(Lnet/minecraft/world/entity/Entity;D)Z"
            )
    )
    private boolean bic$throttleRangedMoveTo(PathNavigation navigation, Entity targetEntity, double speedModifier) {
        if (!BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.enableGlobalFixes)
                || !BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.optimizeRangedAttackGoals)) {
            return navigation.moveTo(targetEntity, speedModifier);
        }
        if (this.bic$repathCooldown > 0) {
            this.bic$repathCooldown--;
            return true;
        }
        double distSqr = this.mob.distanceToSqr(targetEntity);
        int delay = 4 + this.mob.getRandom().nextInt(7);
        if (distSqr > 1024.0) {
            delay += 10;
        } else if (distSqr > 256.0) {
            delay += 5;
        }
        boolean success = navigation.moveTo(targetEntity, speedModifier);
        if (!success) {
            delay += 10;
        }
        this.bic$repathCooldown = delay;
        return success;
    }
}
