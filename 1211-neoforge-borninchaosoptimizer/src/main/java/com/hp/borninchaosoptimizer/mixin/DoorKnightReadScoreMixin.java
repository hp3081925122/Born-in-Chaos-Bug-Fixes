package com.hp.borninchaosoptimizer.mixin;

import com.hp.borninchaosoptimizer.config.OptimizerConfig;
import net.mcreator.borninchaosv.entity.DoorKnightEntity;
import net.mcreator.borninchaosv.entity.DoorKnightNotDespawnEntity;
import net.mcreator.borninchaosv.init.BornInChaosV1ModMobEffects;
import net.mcreator.borninchaosv.procedures.ZombieswithDoorPriObnovlieniiTaktaSushchnostiProcedure;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.Event;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 修复门骑士受击播放格挡动画时读写全局计分板的问题，改用实体自身 NBT 标记。
 */
@Mixin(value = ZombieswithDoorPriObnovlieniiTaktaSushchnostiProcedure.class, remap = false)
public abstract class DoorKnightReadScoreMixin {
    @Inject(method = "execute(Lnet/neoforged/bus/api/Event;Lnet/minecraft/world/entity/Entity;)V", at = @At("HEAD"), cancellable = true)
    private static void bic$replaceScoreboardRead(Event event, Entity entity, CallbackInfo ci) {
        if (!OptimizerConfig.isEnabled(OptimizerConfig.FIX_SCOREBOARD_LEAK)) {
            return;
        }
        ci.cancel();
        if (!(entity instanceof LivingEntity living)) {
            return;
        }
        if (entity instanceof DoorKnightEntity doorKnight) {
            if (living.hasEffect(MobEffects.DAMAGE_RESISTANCE)
                    && !living.hasEffect(BornInChaosV1ModMobEffects.BLOCK_BREAK)
                    && !entity.isOnFire()
                    && entity.getPersistentData().getBoolean("bic_bloc")) {
                entity.getPersistentData().putBoolean("bic_bloc", false);
                doorKnight.setAnimation("block");
            }
        } else if (entity instanceof DoorKnightNotDespawnEntity doorKnightNd) {
            if (living.hasEffect(MobEffects.DAMAGE_RESISTANCE)
                    && !living.hasEffect(BornInChaosV1ModMobEffects.BLOCK_BREAK)
                    && !entity.isOnFire()
                    && entity.getPersistentData().getBoolean("bic_bloc")) {
                entity.getPersistentData().putBoolean("bic_bloc", false);
                doorKnightNd.setAnimation("block");
            }
        }
    }
}
