package com.hp.borninchaosoptimizer.mixin.entity;

import com.hp.borninchaosoptimizer.api.BicRefreshGuard;
import com.hp.borninchaosoptimizer.config.BicOptimizerConfig;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 拦截生于混沌 83 个实体在 baseTick 中每 tick 无条件执行的冗余 refreshDimensions()：
 * 仅在 baseTick 作用域内且姿态、幼年状态均未改变时跳过重算；
 * 首次初始化、姿态切换以及任何外部模组（如体型缩放模组或脚本）主动调用的 refreshDimensions 均完整放行。
 */
@Mixin(Entity.class)
public abstract class RedundantRefreshDimensionsMixin implements BicRefreshGuard {
    @Unique
    private boolean bic$inBicBaseTick;

    @Unique
    private boolean bic$dimensionsInitialized;

    @Unique
    private Pose bic$lastRefreshedPose;

    @Unique
    private boolean bic$lastRefreshedBaby;

    @Override
    public void bic$setInBicBaseTick(boolean value) {
        this.bic$inBicBaseTick = value;
    }

    @Inject(method = "refreshDimensions", at = @At("HEAD"), cancellable = true)
    private void bic$skipPerTickRefreshDimensions(CallbackInfo ci) {
        Entity self = (Entity) (Object) this;
        Pose currentPose = self.getPose();
        boolean currentBaby = self instanceof LivingEntity living && living.isBaby();
        if (this.bic$inBicBaseTick
                && this.bic$dimensionsInitialized
                && currentPose == this.bic$lastRefreshedPose
                && currentBaby == this.bic$lastRefreshedBaby
                && BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.enableGlobalFixes)
                && BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.skipRedundantRefreshDimensions)) {
            ci.cancel();
            return;
        }
        this.bic$dimensionsInitialized = true;
        this.bic$lastRefreshedPose = currentPose;
        this.bic$lastRefreshedBaby = currentBaby;
    }
}
