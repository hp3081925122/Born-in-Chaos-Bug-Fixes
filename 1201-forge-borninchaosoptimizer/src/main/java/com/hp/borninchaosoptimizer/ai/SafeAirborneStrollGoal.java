package com.hp.borninchaosoptimizer.ai;

import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

/**
 * 安全版空中随机巡逻 Goal，仅在空气方块中选取巡逻点，避免向地下实心岩层发起必败三维 A* 搜索。
 */
public final class SafeAirborneStrollGoal extends RandomStrollGoal {
    public SafeAirborneStrollGoal(PathfinderMob mob, double speedModifier, int interval) {
        super(mob, speedModifier, interval);
    }

    @Nullable
    @Override
    protected Vec3 getPosition() {
        return AirborneStrollHelper.findAirborneStrollPos(this.mob);
    }
}
