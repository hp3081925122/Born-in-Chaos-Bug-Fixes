package com.hp.borninchaosoptimizer.mixin;

import com.hp.borninchaosoptimizer.config.OptimizerConfig;
import com.hp.borninchaosoptimizer.util.EntityEffectHelper;
import net.mcreator.borninchaosv.init.BornInChaosV1ModMobEffects;
import net.mcreator.borninchaosv.init.BornInChaosV1ModParticleTypes;
import net.mcreator.borninchaosv.procedures.CorpseFishPriObnovlieniiTikaSushchnostiProcedure;
import net.mcreator.borninchaosv.procedures.GluttonFishLandProcedure;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * 优化腐尸鱼与暴食鱼在水中每刻无条件调用 removeEffect（触发事件总线风暴）与重复刷新海豚的恩惠效果的问题。
 */
@Mixin(value = {
        CorpseFishPriObnovlieniiTikaSushchnostiProcedure.class,
        GluttonFishLandProcedure.class
}, remap = false)
public abstract class AquaticFishTickOptimizeMixin {
    @Redirect(
            method = "execute",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/LivingEntity;removeEffect(Lnet/minecraft/core/Holder;)Z"
            )
    )
    private static boolean bic$removeOnlyIfPresent(LivingEntity entity, Holder<MobEffect> effect) {
        if (!OptimizerConfig.isEnabled(OptimizerConfig.OPTIMIZE_SKY_AND_SUNBURN_CHECKS)) {
            return entity.removeEffect(effect);
        }
        if (entity.hasEffect(effect)) {
            return entity.removeEffect(effect);
        }
        return false;
    }

    @Redirect(
            method = "execute",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/LivingEntity;addEffect(Lnet/minecraft/world/effect/MobEffectInstance;)Z"
            )
    )
    private static boolean bic$throttleDolphinsGrace(LivingEntity entity, MobEffectInstance instance) {
        if (OptimizerConfig.isEnabled(OptimizerConfig.OPTIMIZE_SKY_AND_SUNBURN_CHECKS)
                && instance.is(MobEffects.DOLPHINS_GRACE)) {
            EntityEffectHelper.refreshPassiveEffect(entity, MobEffects.DOLPHINS_GRACE, instance.getDuration(), instance.getAmplifier(), 8);
            return true;
        }
        return entity.addEffect(instance);
    }
}
