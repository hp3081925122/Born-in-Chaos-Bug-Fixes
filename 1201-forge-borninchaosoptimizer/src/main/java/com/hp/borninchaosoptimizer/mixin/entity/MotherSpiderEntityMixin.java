package com.hp.borninchaosoptimizer.mixin.entity;

import com.hp.borninchaosoptimizer.config.BicOptimizerConfig;
import net.mcreator.borninchaosv.entity.BabySpiderEntity;
import net.mcreator.borninchaosv.entity.MotherSpiderEntity;
import net.mcreator.borninchaosv.procedures.MotherSpiderSummonProcedure;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.SynchedEntityData;
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
 * 修复 MotherSpiderEntity 的两项严重缺陷：
 * 1. DATA_FLAGS_ID 误传 BabySpiderEntity.class 导致跨类 SynchedEntityData ID 错位与客户端解码崩溃；
 * 2. hurt() 在摔落免疫及 super.hurt 无敌帧判定之前无条件调用召唤过程。
 */
@Mixin(MotherSpiderEntity.class)
public abstract class MotherSpiderEntityMixin extends Monster {
    protected MotherSpiderEntityMixin(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Redirect(
            method = "<clinit>",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/network/syncher/SynchedEntityData;defineId(Ljava/lang/Class;Lnet/minecraft/network/syncher/EntityDataSerializer;)Lnet/minecraft/network/syncher/EntityDataAccessor;"
            )
    )
    private static EntityDataAccessor<?> bic$fixWrongSynchedDataOwner(Class<? extends Entity> entityClass, EntityDataSerializer<?> serializer) {
        if (entityClass == BabySpiderEntity.class
                && BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.fixMotherSpiderSynchedData)) {
            return SynchedEntityData.defineId(MotherSpiderEntity.class, (EntityDataSerializer) serializer);
        }
        return SynchedEntityData.defineId((Class) entityClass, (EntityDataSerializer) serializer);
    }

    @Inject(method = "hurt", at = @At("HEAD"), cancellable = true)
    private void bic$orderedHurt(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (!BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.enableGlobalFixes)
                || !BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.fixEntityHurtOrder)) {
            return;
        }
        if (source.is(DamageTypes.FALL)) {
            cir.setReturnValue(false);
            return;
        }
        boolean damaged = super.hurt(source, amount);
        if (damaged && !this.level().isClientSide()) {
            MotherSpiderSummonProcedure.execute(this.level(), this.getY(), this);
        }
        cir.setReturnValue(damaged);
    }
}
