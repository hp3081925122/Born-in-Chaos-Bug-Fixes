package com.hp.borninchaosoptimizer.mixin;

import com.hp.borninchaosoptimizer.config.OptimizerConfig;
import net.mcreator.borninchaosv.entity.KrampusEntity;
import net.mcreator.borninchaosv.entity.LifestealerTrueFormEntity;
import net.mcreator.borninchaosv.entity.LordPumpkinheadEntity;
import net.mcreator.borninchaosv.entity.LordPumpkinheadHeadEntity;
import net.mcreator.borninchaosv.entity.LordPumpkinheadWithoutaHorseEntity;
import net.mcreator.borninchaosv.entity.MotherSpiderEntity;
import net.mcreator.borninchaosv.entity.NightmareStalkerEntity;
import net.mcreator.borninchaosv.entity.PhantomCreeperEntity;
import net.mcreator.borninchaosv.entity.PumpkinheadEntity;
import net.mcreator.borninchaosv.entity.SirPumpkinheadEntity;
import net.mcreator.borninchaosv.entity.SirPumpkinheadWithoutHorseEntity;
import net.mcreator.borninchaosv.procedures.KrampusPriRanieniiSushchnostiProcedure;
import net.mcreator.borninchaosv.procedures.LifestealerTrueFormPriRanieniiSushchnostiProcedure;
import net.mcreator.borninchaosv.procedures.LordPumpkinheadHeadhitProcedure;
import net.mcreator.borninchaosv.procedures.LordPumpkinheadPriRanieniiSushchnostiProcedure;
import net.mcreator.borninchaosv.procedures.LordPumpkinheadhitProcedure;
import net.mcreator.borninchaosv.procedures.MotherSpiderSummonProcedure;
import net.mcreator.borninchaosv.procedures.NightmareStalkerPriRanieniiSushchnostiProcedure;
import net.mcreator.borninchaosv.procedures.PumpkinheadTelProcedure;
import net.mcreator.borninchaosv.procedures.SerPumpkinheadPiProcedure;
import net.mcreator.borninchaosv.procedures.SerPumpkinheadWTelProcedure;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 修复母蜘蛛、梦魇潜行者、幻影苦力怕及各阶段首领在判断伤害免疫与 super.hurt（无敌帧/客户端）之前提前触发受击过程的同源缺陷：
 * 1. 防止母蜘蛛跳崖免疫摔落时误刷幼蛛、幻影苦力怕免疫爆炸时误分裂消失；
 * 2. 防止梦魇潜行者、南瓜领主三形态、南瓜爵士二形态、南瓜头、坎卜斯、窃命者真身在免疫箭矢/火焰/摔落/爆炸或无敌帧期间误触发瞬移刷怪、攒怒与大招。
 */
@Mixin({
        MotherSpiderEntity.class,
        NightmareStalkerEntity.class,
        PhantomCreeperEntity.class,
        KrampusEntity.class,
        LifestealerTrueFormEntity.class,
        LordPumpkinheadEntity.class,
        LordPumpkinheadWithoutaHorseEntity.class,
        LordPumpkinheadHeadEntity.class,
        SirPumpkinheadEntity.class,
        SirPumpkinheadWithoutHorseEntity.class,
        PumpkinheadEntity.class
})
public abstract class EntityHurtOrderFixMixin extends Monster {
    protected EntityHurtOrderFixMixin(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(method = "hurt", at = @At("HEAD"), cancellable = true)
    private void bic$checkImmunityAndHurtBeforeTrigger(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (!OptimizerConfig.isEnabled(OptimizerConfig.FIX_ENTITY_HURT_ORDER_AND_SUMMON_PARTICLE)) {
            return;
        }
        Object self = this;
        if (self instanceof MotherSpiderEntity) {
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
        if (self instanceof NightmareStalkerEntity) {
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
            return;
        }
        if (self instanceof PhantomCreeperEntity) {
            if (source.is(DamageTypeTags.IS_EXPLOSION)
                    || source.is(DamageTypes.DRAGON_BREATH)
                    || this.isInvulnerableTo(source)
                    || this.level().isClientSide()) {
                cir.setReturnValue(false);
            }
        }
    }

    @Redirect(
            method = "hurt",
            require = 0,
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/mcreator/borninchaosv/procedures/KrampusPriRanieniiSushchnostiProcedure;execute(Lnet/minecraft/world/level/LevelAccessor;DDDLnet/minecraft/world/entity/Entity;Lnet/minecraft/world/entity/Entity;)V",
                    remap = false
            )
    )
    private void bic$deferKrampusHurt(LevelAccessor world, double x, double y, double z, Entity entity, Entity sourceentity) {
        if (!OptimizerConfig.isEnabled(OptimizerConfig.FIX_ENTITY_HURT_ORDER_AND_SUMMON_PARTICLE)) {
            KrampusPriRanieniiSushchnostiProcedure.execute(world, x, y, z, entity, sourceentity);
        }
    }

    @Redirect(
            method = "hurt",
            require = 0,
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/mcreator/borninchaosv/procedures/LifestealerTrueFormPriRanieniiSushchnostiProcedure;execute(Lnet/minecraft/world/level/LevelAccessor;DDDLnet/minecraft/world/entity/Entity;Lnet/minecraft/world/entity/Entity;)V",
                    remap = false
            )
    )
    private void bic$deferLifestealerHurt(LevelAccessor world, double x, double y, double z, Entity entity, Entity sourceentity) {
        if (!OptimizerConfig.isEnabled(OptimizerConfig.FIX_ENTITY_HURT_ORDER_AND_SUMMON_PARTICLE)) {
            LifestealerTrueFormPriRanieniiSushchnostiProcedure.execute(world, x, y, z, entity, sourceentity);
        }
    }

    @Redirect(
            method = "hurt",
            require = 0,
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/mcreator/borninchaosv/procedures/LordPumpkinheadPriRanieniiSushchnostiProcedure;execute(Lnet/minecraft/world/level/LevelAccessor;DDDLnet/minecraft/world/entity/Entity;)V",
                    remap = false
            )
    )
    private void bic$deferLordMountedHurt(LevelAccessor world, double x, double y, double z, Entity entity) {
        if (!OptimizerConfig.isEnabled(OptimizerConfig.FIX_ENTITY_HURT_ORDER_AND_SUMMON_PARTICLE)) {
            LordPumpkinheadPriRanieniiSushchnostiProcedure.execute(world, x, y, z, entity);
        }
    }

    @Redirect(
            method = "hurt",
            require = 0,
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/mcreator/borninchaosv/procedures/LordPumpkinheadhitProcedure;execute(Lnet/minecraft/world/level/LevelAccessor;DDDLnet/minecraft/world/entity/Entity;)V",
                    remap = false
            )
    )
    private void bic$deferLordUnmountedHurt(LevelAccessor world, double x, double y, double z, Entity entity) {
        if (!OptimizerConfig.isEnabled(OptimizerConfig.FIX_ENTITY_HURT_ORDER_AND_SUMMON_PARTICLE)) {
            LordPumpkinheadhitProcedure.execute(world, x, y, z, entity);
        }
    }

    @Redirect(
            method = "hurt",
            require = 0,
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/mcreator/borninchaosv/procedures/LordPumpkinheadHeadhitProcedure;execute(Lnet/minecraft/world/level/LevelAccessor;DDDLnet/minecraft/world/entity/Entity;)V",
                    remap = false
            )
    )
    private void bic$deferLordHeadHurt(LevelAccessor world, double x, double y, double z, Entity entity) {
        if (!OptimizerConfig.isEnabled(OptimizerConfig.FIX_ENTITY_HURT_ORDER_AND_SUMMON_PARTICLE)) {
            LordPumpkinheadHeadhitProcedure.execute(world, x, y, z, entity);
        }
    }

    @Redirect(
            method = "hurt",
            require = 0,
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/mcreator/borninchaosv/procedures/SerPumpkinheadPiProcedure;execute(Lnet/minecraft/world/level/LevelAccessor;DDDLnet/minecraft/world/entity/Entity;)V",
                    remap = false
            )
    )
    private void bic$deferSirMountedHurt(LevelAccessor world, double x, double y, double z, Entity entity) {
        if (!OptimizerConfig.isEnabled(OptimizerConfig.FIX_ENTITY_HURT_ORDER_AND_SUMMON_PARTICLE)) {
            SerPumpkinheadPiProcedure.execute(world, x, y, z, entity);
        }
    }

    @Redirect(
            method = "hurt",
            require = 0,
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/mcreator/borninchaosv/procedures/SerPumpkinheadWTelProcedure;execute(Lnet/minecraft/world/level/LevelAccessor;DDDLnet/minecraft/world/entity/Entity;)V",
                    remap = false
            )
    )
    private void bic$deferSirUnmountedHurt(LevelAccessor world, double x, double y, double z, Entity entity) {
        if (!OptimizerConfig.isEnabled(OptimizerConfig.FIX_ENTITY_HURT_ORDER_AND_SUMMON_PARTICLE)) {
            SerPumpkinheadWTelProcedure.execute(world, x, y, z, entity);
        }
    }

    @Redirect(
            method = "hurt",
            require = 0,
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/mcreator/borninchaosv/procedures/PumpkinheadTelProcedure;execute(Lnet/minecraft/world/level/LevelAccessor;DDDLnet/minecraft/world/entity/Entity;)V",
                    remap = false
            )
    )
    private void bic$deferPumpkinheadHurt(LevelAccessor world, double x, double y, double z, Entity entity) {
        if (!OptimizerConfig.isEnabled(OptimizerConfig.FIX_ENTITY_HURT_ORDER_AND_SUMMON_PARTICLE)) {
            PumpkinheadTelProcedure.execute(world, x, y, z, entity);
        }
    }

    @Inject(method = "hurt", at = @At("RETURN"))
    private void bic$runHurtProcedureAfterActualDamage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (!OptimizerConfig.isEnabled(OptimizerConfig.FIX_ENTITY_HURT_ORDER_AND_SUMMON_PARTICLE)) {
            return;
        }
        if (!cir.getReturnValueZ() || !this.isAlive() || this.level().isClientSide()) {
            return;
        }
        Object self = this;
        if (self instanceof KrampusEntity) {
            KrampusPriRanieniiSushchnostiProcedure.execute(this.level(), this.getX(), this.getY(), this.getZ(), this, source.getEntity());
        } else if (self instanceof LifestealerTrueFormEntity) {
            LifestealerTrueFormPriRanieniiSushchnostiProcedure.execute(this.level(), this.getX(), this.getY(), this.getZ(), this, source.getEntity());
        } else if (self instanceof LordPumpkinheadEntity) {
            LordPumpkinheadPriRanieniiSushchnostiProcedure.execute(this.level(), this.getX(), this.getY(), this.getZ(), this);
        } else if (self instanceof LordPumpkinheadWithoutaHorseEntity) {
            LordPumpkinheadhitProcedure.execute(this.level(), this.getX(), this.getY(), this.getZ(), this);
        } else if (self instanceof LordPumpkinheadHeadEntity) {
            LordPumpkinheadHeadhitProcedure.execute(this.level(), this.getX(), this.getY(), this.getZ(), this);
        } else if (self instanceof SirPumpkinheadEntity) {
            SerPumpkinheadPiProcedure.execute(this.level(), this.getX(), this.getY(), this.getZ(), this);
        } else if (self instanceof SirPumpkinheadWithoutHorseEntity) {
            SerPumpkinheadWTelProcedure.execute(this.level(), this.getX(), this.getY(), this.getZ(), this);
        } else if (self instanceof PumpkinheadEntity) {
            PumpkinheadTelProcedure.execute(this.level(), this.getX(), this.getY(), this.getZ(), this);
        }
    }
}
