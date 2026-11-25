package com.hp.borninchaosoptimizer.mixin.procedure;

import com.hp.borninchaosoptimizer.config.BicOptimizerConfig;
import net.mcreator.borninchaosv.init.BornInChaosV1ModMobEffects;
import net.mcreator.borninchaosv.procedures.CorpseFishPriObnovlieniiTikaSushchnostiProcedure;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 修复尸鱼每 tick 过程：仅服务端执行，并在水中先检查 hasEffect 再移除药水，消除水中大量尸鱼每 tick 双次刷爆事件总线。
 */
@Mixin(value = CorpseFishPriObnovlieniiTikaSushchnostiProcedure.class, remap = false)
public abstract class CorpseFishTickProcedureMixin {
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

        MobEffect fishBreath = BornInChaosV1ModMobEffects.FISH_BREATH.get();
        if (!entity.isInWater() && !living.hasEffect(fishBreath)) {
            living.addEffect(new MobEffectInstance(fishBreath, 300, 0, false, false));
        } else if (entity.isInWater()) {
            if (living.hasEffect(MobEffects.WITHER)) {
                living.removeEffect(MobEffects.WITHER);
            }
            if (living.hasEffect(fishBreath)) {
                living.removeEffect(fishBreath);
            }
            MobEffectInstance grace = living.getEffect(MobEffects.DOLPHINS_GRACE);
            if (!BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.optimizeSubEffectRefreshes)
                    || grace == null
                    || grace.getDuration() <= 10) {
                living.addEffect(new MobEffectInstance(MobEffects.DOLPHINS_GRACE, 40, 0, false, false));
            }
        }

        MobEffectInstance breathInst = living.getEffect(fishBreath);
        if (breathInst != null && breathInst.getDuration() == 20) {
            living.addEffect(new MobEffectInstance(MobEffects.WITHER, 300, 1, false, false));
        }
    }
}
