package com.hp.borninchaosoptimizer.mixin.procedure;

import com.hp.borninchaosoptimizer.config.BicOptimizerConfig;
import net.mcreator.borninchaosv.procedures.BabySpiderControlledPriObnovlieniiTikaSushchnostiProcedure;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 修复受控幼蛛每 tick 过程：仅服务端执行，且仅在携带中毒时才调用 removeEffect，消除事件总线风暴。
 */
@Mixin(value = BabySpiderControlledPriObnovlieniiTikaSushchnostiProcedure.class, remap = false)
public abstract class BabySpiderControlledTickMixin {
    @Inject(method = "execute", at = @At("HEAD"), cancellable = true)
    private static void bicOpt$safeTick(Entity entity, CallbackInfo ci) {
        if (!BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.enableGlobalFixes)
                || !BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.fixEntityTickClientSideMutations)) {
            return;
        }
        ci.cancel();
        if (entity == null || entity.level().isClientSide()) {
            return;
        }
        if (entity instanceof LivingEntity living && living.hasEffect(MobEffects.POISON)) {
            living.removeEffect(MobEffects.POISON);
        }
        if (entity.getPersistentData().getBoolean("attack_target")) {
            double cur = entity.getPersistentData().getDouble("target");
            double next = cur == 0.0 ? 10.0 : cur - 1.0;
            entity.getPersistentData().putDouble("target", next);
            if (next == 0.0) {
                entity.getPersistentData().putBoolean("attack_target", false);
            }
        }
    }
}
