package com.hp.borninchaosoptimizer.mixin;

import com.hp.borninchaosoptimizer.config.OptimizerConfig;
import javax.annotation.Nullable;
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

/**
 * 统一优化原模组全部 13 个内部 RangedAttackGoal 类：
 * 1. 增加目标判空与存活校验，防止奇数刻或目标清空后触发空指针；
 * 2. 对标原版 MeleeAttackGoal 为远程追击的 navigation.moveTo() 增加动态重算间隔，消除目标移动或不可达时每刻全量重跑 A* 的开销。
 */
@Mixin(targets = {
        "net.mcreator.borninchaosv.entity.BonescallerEntity$RangedAttackGoal",
        "net.mcreator.borninchaosv.entity.BonescallerNotDespawnEntity$RangedAttackGoal",
        "net.mcreator.borninchaosv.entity.DarkVortexEntity$RangedAttackGoal",
        "net.mcreator.borninchaosv.entity.LordPumpkinheadHeadEntity$RangedAttackGoal",
        "net.mcreator.borninchaosv.entity.LordPumpkinheadWithoutaHorseEntity$RangedAttackGoal",
        "net.mcreator.borninchaosv.entity.MissionaryRaiderEntity$RangedAttackGoal",
        "net.mcreator.borninchaosv.entity.MissionerEntity$RangedAttackGoal",
        "net.mcreator.borninchaosv.entity.MrsPumpkinEntity$RangedAttackGoal",
        "net.mcreator.borninchaosv.entity.PumpkinheadEntity$RangedAttackGoal",
        "net.mcreator.borninchaosv.entity.SirPumpkinheadWithoutHorseEntity$RangedAttackGoal",
        "net.mcreator.borninchaosv.entity.SupremeBonescallerEntity$RangedAttackGoal",
        "net.mcreator.borninchaosv.entity.SupremeBonescallerNotDespawnEntity$RangedAttackGoal",
        "net.mcreator.borninchaosv.entity.SupremeBonescallerStage2Entity$RangedAttackGoal"
})
public abstract class CustomRangedGoalOptimizeMixin {
    @Shadow(remap = false)
    @Final
    private Mob mob;

    @Shadow(remap = false)
    @Nullable
    private LivingEntity target;

    @Unique
    private int bic$repathCooldown = 0;

    @Inject(method = "stop", at = @At("TAIL"))
    private void bic$resetCooldownOnStop(CallbackInfo ci) {
        this.bic$repathCooldown = 0;
    }

    @Inject(method = "canContinueToUse", at = @At("HEAD"), cancellable = true)
    private void bic$guardNullTargetInContinue(CallbackInfoReturnable<Boolean> cir) {
        if (OptimizerConfig.isEnabled(OptimizerConfig.FIX_CUSTOM_GOAL_NULL_TARGET)) {
            if (this.target == null && this.mob.getTarget() == null) {
                cir.setReturnValue(false);
            }
        }
    }

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void bic$guardNullTargetInTick(CallbackInfo ci) {
        if (OptimizerConfig.isEnabled(OptimizerConfig.FIX_CUSTOM_GOAL_NULL_TARGET)) {
            if (this.target == null || !this.target.isAlive()) {
                ci.cancel();
            }
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
        if (!OptimizerConfig.isEnabled(OptimizerConfig.FIX_CUSTOM_GOAL_NULL_TARGET)) {
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
