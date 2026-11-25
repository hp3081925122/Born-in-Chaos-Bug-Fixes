package com.hp.borninchaosoptimizer.mixin;

import com.hp.borninchaosoptimizer.config.OptimizerConfig;
import net.mcreator.borninchaosv.entity.MotherSpiderEntity;
import net.mcreator.borninchaosv.entity.NightmareStalkerEntity;
import net.mcreator.borninchaosv.procedures.MotherSpiderSummonProcedure;
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
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 修复母蜘蛛与梦魇潜行者在判断免疫伤害类型（摔落/箭矢/溺水/三叉戟/凋零）与 super.hurt（无敌帧/客户端）之前就提前调用受击过程的缺陷：
 * 防止母蜘蛛跳下悬崖免疫摔落时凭空刷小蜘蛛，并防止玩家用免疫的弓箭远程白攒梦魇潜行者狂暴值或被水下抽空氧气。
 */
@Mixin(value = {MotherSpiderEntity.class, NightmareStalkerEntity.class})
public abstract class EntityHurtOrderFixMixin extends Monster {
    protected EntityHurtOrderFixMixin(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(method = "hurt", at = @At("HEAD"), cancellable = true)
    private void bic$checkImmunityAndHurtBeforeTrigger(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (!OptimizerConfig.isEnabled(OptimizerConfig.FIX_ENTITY_HURT_ORDER_AND_SUMMON_PARTICLE)) {
            return;
        }
        if ((Object) this instanceof MotherSpiderEntity) {
            if (source.is(DamageTypes.FALL)) {
                cir.setReturnValue(false);
                return;
            }
            boolean damaged = super.hurt(source, amount);
            if (damaged && !this.level().isClientSide()) {
                MotherSpiderSummonProcedure.execute(this.level(), this.getY(), this);
            }
            cir.setReturnValue(damaged);
            return;
        }
        if ((Object) this instanceof NightmareStalkerEntity) {
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
}
