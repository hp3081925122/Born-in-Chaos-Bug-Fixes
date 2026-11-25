package com.hp.borninchaosoptimizer.mixin.entity;

import com.hp.borninchaosoptimizer.ai.SegmentedGroundPathNavigation;
import com.hp.borninchaosoptimizer.config.BicOptimizerConfig;
import net.mcreator.borninchaosv.entity.NightmareStalkerEntity;
import net.mcreator.borninchaosv.procedures.NightmareStalkerPriRanieniiSushchnostiProcedure;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 优化与修复 NightmareStalkerEntity：
 * 1. 为其 80 格 FOLLOW_RANGE 注入分段地面寻路器，消除隔墙不可达时单次硬搜 1280 个地面节点造成的卡顿；
 * 2. 修复 hurt() 在箭矢/三叉戟/摔落/溺水/凋零免疫判断之前误触发受击怒气与破船逻辑的问题。
 */
@Mixin(NightmareStalkerEntity.class)
public abstract class NightmareStalkerEntityMixin extends Monster {
    protected NightmareStalkerEntityMixin(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(
            method = "<init>(Lnet/minecraft/world/entity/EntityType;Lnet/minecraft/world/level/Level;)V",
            at = @At("TAIL")
    )
    private void bic$installSegmentedGroundNav(EntityType<NightmareStalkerEntity> type, Level world, CallbackInfo ci) {
        if (world != null
                && BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.enableGlobalFixes)
                && BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.optimizeEntityPathfinding)) {
            this.navigation = new SegmentedGroundPathNavigation(this, world);
        }
    }

    @Inject(method = "hurt", at = @At("HEAD"), cancellable = true)
    private void bic$orderedHurt(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (!BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.enableGlobalFixes)
                || !BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.fixEntityHurtOrder)) {
            return;
        }
        if (source.getDirectEntity() instanceof AbstractArrow
                || source.is(DamageTypes.FALL)
                || source.is(DamageTypes.DROWN)
                || source.is(DamageTypes.TRIDENT)
                || source.is(DamageTypes.WITHER)
                || source.is(DamageTypes.WITHER_SKULL)) {
            cir.setReturnValue(false);
            return;
        }
        boolean damaged = super.hurt(source, amount);
        if (damaged && !this.level().isClientSide()) {
            NightmareStalkerPriRanieniiSushchnostiProcedure.execute(
                    this.level(), this.getX(), this.getY(), this.getZ(), this, source.getEntity()
            );
        }
        cir.setReturnValue(damaged);
    }
}
