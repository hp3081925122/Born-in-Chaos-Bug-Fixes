package com.hp.borninchaosoptimizer.mixin.scoreboard;

import com.hp.borninchaosoptimizer.config.BicOptimizerConfig;
import net.mcreator.borninchaosv.entity.DoorKnightEntity;
import net.mcreator.borninchaosv.entity.DoorKnightNotDespawnEntity;
import net.mcreator.borninchaosv.init.BornInChaosV1ModMobEffects;
import net.mcreator.borninchaosv.procedures.ZombieswithDoorPriObnovlieniiTaktaSushchnostiProcedure;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.eventbus.api.Event;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 替换门骑士格挡动画触发器对原版 Scoreboard("bloc") 的读写，使用实体 PersistentData。
 */
@Mixin(value = ZombieswithDoorPriObnovlieniiTaktaSushchnostiProcedure.class, remap = false)
public abstract class ZombieswithDoorAttackProcedureMixin {
    @Unique
    private static final String BIC_KEY_BLOC = "bic_opt_bloc";

    @Inject(
            method = "execute(Lnet/minecraftforge/eventbus/api/Event;Lnet/minecraft/world/entity/Entity;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void bic$replaceScoreboardRead(Event event, Entity entity, CallbackInfo ci) {
        if (!BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.enableGlobalFixes)
                || !BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.fixScoreboardEntityLeak)) {
            return;
        }
        ci.cancel();
        if (entity == null || entity.level().isClientSide()) {
            return;
        }
        if (!(entity instanceof DoorKnightEntity || entity instanceof DoorKnightNotDespawnEntity)) {
            return;
        }
        if (!(entity instanceof LivingEntity living)) {
            return;
        }
        if (!living.hasEffect(MobEffects.DAMAGE_RESISTANCE)
                || living.hasEffect(BornInChaosV1ModMobEffects.BLOCK_BREAK.get())
                || entity.isOnFire()) {
            return;
        }
        if (entity.getPersistentData().getBoolean(BIC_KEY_BLOC)) {
            entity.getPersistentData().putBoolean(BIC_KEY_BLOC, false);
            if (entity instanceof DoorKnightEntity doorKnight) {
                doorKnight.setAnimation("block");
            } else if (entity instanceof DoorKnightNotDespawnEntity doorKnightNd) {
                doorKnightNd.setAnimation("block");
            }
        }
    }
}
