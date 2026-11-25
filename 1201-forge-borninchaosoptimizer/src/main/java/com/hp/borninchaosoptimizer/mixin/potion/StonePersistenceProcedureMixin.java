package com.hp.borninchaosoptimizer.mixin.potion;

import com.hp.borninchaosoptimizer.config.BicOptimizerConfig;
import net.mcreator.borninchaosv.init.BornInChaosV1ModMobEffects;
import net.mcreator.borninchaosv.procedures.StonePersistenceKazhdyiTikVoVriemiaEffiektaProcedure;
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
 * 修复磐石坚韧每 tick 在双端无条件调用 removeEffect(STUN) 刷爆 Forge 事件总线的问题。
 */
@Mixin(value = StonePersistenceKazhdyiTikVoVriemiaEffiektaProcedure.class, remap = false)
public abstract class StonePersistenceProcedureMixin {
    @Inject(method = "execute", at = @At("HEAD"), cancellable = true)
    private static void bicOptimizer$optimizeStonePersistence(Entity entity, CallbackInfo ci) {
        if (!BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.enableGlobalFixes)
                || !BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.optimizePotionAuraAndStateTicks)) {
            return;
        }
        ci.cancel();
        if (!(entity instanceof LivingEntity living) || living.level().isClientSide()) {
            return;
        }
        MobEffectInstance currentRes = living.getEffect(MobEffects.DAMAGE_RESISTANCE);
        if (!BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.optimizeSubEffectRefreshes)
                || currentRes == null
                || currentRes.getAmplifier() < 3
                || currentRes.getDuration() <= 3) {
            living.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 10, 3, false, false));
        }
        MobEffect stun = BornInChaosV1ModMobEffects.STUN.get();
        if (living.hasEffect(stun)) {
            living.removeEffect(stun);
        }
    }
}
