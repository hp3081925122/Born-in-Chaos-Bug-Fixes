package com.hp.borninchaosoptimizer.mixin.potion;

import com.hp.borninchaosoptimizer.config.BicOptimizerConfig;
import net.mcreator.borninchaosv.procedures.WitherResistanceKazhdyiTikVoVriemiaEffiektaProcedure;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 修复凋零抗性每 tick 在双端无条件调用 removeEffect(WITHER) 触发每股每秒 40 次 Forge MobEffectEvent.Remove 事件分配的问题。
 */
@Mixin(value = WitherResistanceKazhdyiTikVoVriemiaEffiektaProcedure.class, remap = false)
public abstract class WitherResistanceProcedureMixin {
    @Inject(method = "execute", at = @At("HEAD"), cancellable = true)
    private static void bicOptimizer$optimizeWitherResistance(Entity entity, CallbackInfo ci) {
        if (!BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.enableGlobalFixes)
                || !BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.optimizePotionAuraAndStateTicks)) {
            return;
        }
        ci.cancel();
        if (entity instanceof LivingEntity living && !living.level().isClientSide() && living.hasEffect(MobEffects.WITHER)) {
            living.removeEffect(MobEffects.WITHER);
        }
    }
}
