package com.hp.borninchaosoptimizer.ai;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

/**
 * 安全版空中冲刺攻击 Goal，替代 RestlessSpiritEntity 与 MrPumpkinEntity 原本未对 getTarget() 判空的匿名内部类 Goal，
 * 根治奇数 tick 目标死亡或被清除时抛出的 Ticking entity NullPointerException。
 */
public final class SafeFlyingChargeGoal extends Goal {
    private final Mob mob;
    private final double chargeSpeed;
    private final double retargetDistSqr;

    public SafeFlyingChargeGoal(Mob mob, double chargeSpeed, double retargetDistSqr) {
        this.mob = mob;
        this.chargeSpeed = chargeSpeed;
        this.retargetDistSqr = retargetDistSqr;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        LivingEntity target = this.mob.getTarget();
        return target != null && target.isAlive() && !this.mob.getMoveControl().hasWanted();
    }

    @Override
    public boolean canContinueToUse() {
        LivingEntity target = this.mob.getTarget();
        return this.mob.getMoveControl().hasWanted() && target != null && target.isAlive();
    }

    @Override
    public void start() {
        LivingEntity target = this.mob.getTarget();
        if (target == null || !target.isAlive()) {
            return;
        }
        Vec3 eyePos = target.getEyePosition(1.0f);
        this.mob.getMoveControl().setWantedPosition(eyePos.x, eyePos.y, eyePos.z, this.chargeSpeed);
    }

    @Override
    public void tick() {
        LivingEntity target = this.mob.getTarget();
        if (target == null || !target.isAlive()) {
            return;
        }
        if (this.mob.getBoundingBox().intersects(target.getBoundingBox())) {
            this.mob.doHurtTarget(target);
        } else if (this.mob.distanceToSqr(target) < this.retargetDistSqr) {
            Vec3 eyePos = target.getEyePosition(1.0f);
            this.mob.getMoveControl().setWantedPosition(eyePos.x, eyePos.y, eyePos.z, this.chargeSpeed);
        }
    }
}
