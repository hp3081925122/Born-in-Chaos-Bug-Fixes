package com.hp.borninchaosoptimizer.mixin.entity;

import com.hp.borninchaosoptimizer.config.BicOptimizerConfig;
import net.mcreator.borninchaosv.entity.KrampusEntity;
import net.mcreator.borninchaosv.entity.LifestealerTrueFormEntity;
import net.mcreator.borninchaosv.entity.LordPumpkinheadEntity;
import net.mcreator.borninchaosv.entity.LordPumpkinheadHeadEntity;
import net.mcreator.borninchaosv.entity.LordPumpkinheadWithoutaHorseEntity;
import net.mcreator.borninchaosv.entity.PhantomCreeperEntity;
import net.mcreator.borninchaosv.entity.PumpkinheadEntity;
import net.mcreator.borninchaosv.entity.SirPumpkinheadEntity;
import net.mcreator.borninchaosv.entity.SirPumpkinheadWithoutHorseEntity;
import net.mcreator.borninchaosv.procedures.KrampusPriRanieniiSushchnostiProcedure;
import net.mcreator.borninchaosv.procedures.LifestealerTrueFormPriRanieniiSushchnostiProcedure;
import net.mcreator.borninchaosv.procedures.LordPumpkinheadHeadhitProcedure;
import net.mcreator.borninchaosv.procedures.LordPumpkinheadPriRanieniiSushchnostiProcedure;
import net.mcreator.borninchaosv.procedures.LordPumpkinheadhitProcedure;
import net.mcreator.borninchaosv.procedures.PumpkinheadTelProcedure;
import net.mcreator.borninchaosv.procedures.SerPumpkinheadPiProcedure;
import net.mcreator.borninchaosv.procedures.SerPumpkinheadWTelProcedure;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 修复首领与幻影苦力怕在判断伤害免疫及实际扣血之前提前调用受击过程的同源缺陷：
 * 1. 防止幻影苦力怕在免疫爆炸/龙息伤害或无敌状态下白白分裂消失；
 * 2. 防止南瓜领主三形态、南瓜爵士二形态、南瓜头、坎卜斯、窃命者真身在免疫箭矢/火焰/摔落/爆炸或无敌帧期间误触发瞬移刷怪与大招。
 */
@Mixin({
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
public abstract class BossAndCreeperHurtOrderMixin extends Monster {
    protected BossAndCreeperHurtOrderMixin(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(method = "hurt", at = @At("HEAD"), cancellable = true)
    private void bic$guardPhantomCreeperImmuneSplit(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (!BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.enableGlobalFixes)
                || !BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.fixEntityHurtOrder)) {
            return;
        }
        if ((Object) this instanceof PhantomCreeperEntity) {
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
        if (!BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.enableGlobalFixes)
                || !BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.fixEntityHurtOrder)) {
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
        if (!BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.enableGlobalFixes)
                || !BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.fixEntityHurtOrder)) {
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
        if (!BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.enableGlobalFixes)
                || !BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.fixEntityHurtOrder)) {
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
        if (!BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.enableGlobalFixes)
                || !BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.fixEntityHurtOrder)) {
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
        if (!BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.enableGlobalFixes)
                || !BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.fixEntityHurtOrder)) {
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
        if (!BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.enableGlobalFixes)
                || !BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.fixEntityHurtOrder)) {
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
        if (!BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.enableGlobalFixes)
                || !BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.fixEntityHurtOrder)) {
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
        if (!BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.enableGlobalFixes)
                || !BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.fixEntityHurtOrder)) {
            PumpkinheadTelProcedure.execute(world, x, y, z, entity);
        }
    }

    @Inject(method = "hurt", at = @At("RETURN"))
    private void bic$runHurtProcedureAfterActualDamage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (!BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.enableGlobalFixes)
                || !BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.fixEntityHurtOrder)) {
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
