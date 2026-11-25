package com.hp.borninchaosoptimizer.mixin.procedure;

import com.hp.borninchaosoptimizer.config.BicOptimizerConfig;
import net.mcreator.borninchaosv.entity.KrampusHenchmanEntity;
import net.mcreator.borninchaosv.init.BornInChaosV1ModMobEffects;
import net.mcreator.borninchaosv.procedures.KrampusHenchmanPriObnovlieniiTikaSushchnostiProcedure;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 修复克朗普斯爪牙每 tick 过程：仅服务端执行，且先检查 hasEffect 再移除刺骨严寒。
 */
@Mixin(value = KrampusHenchmanPriObnovlieniiTikaSushchnostiProcedure.class, remap = false)
public abstract class KrampusHenchmanTickProcedureMixin {
    @Inject(method = "execute", at = @At("HEAD"), cancellable = true)
    private static void bicOpt$safeTick(Entity entity, CallbackInfo ci) {
        if (!BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.enableGlobalFixes)
                || !BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.fixEntityTickClientSideMutations)) {
            return;
        }
        ci.cancel();
        if (!(entity instanceof LivingEntity living) || entity.level().isClientSide()) {
            return;
        }

        MobEffect boneChilling = BornInChaosV1ModMobEffects.BONE_CHILLING.get();
        if (living.hasEffect(boneChilling)) {
            living.removeEffect(boneChilling);
        }

        MobEffect blockBreak = BornInChaosV1ModMobEffects.BLOCK_BREAK.get();
        if (living.hasEffect(MobEffects.DAMAGE_RESISTANCE) && living.hasEffect(blockBreak)) {
            double cur = entity.getPersistentData().getDouble("appearance");
            double next = cur == 0.0 ? 1.0 : cur - 1.0;
            entity.getPersistentData().putDouble("appearance", next);
            if (next == 0.0) {
                if (entity instanceof KrampusHenchmanEntity henchman) {
                    henchman.setAnimation("appearance");
                }
                living.removeEffect(MobEffects.DAMAGE_RESISTANCE);
                living.removeEffect(blockBreak);
            }
        }
    }
}
