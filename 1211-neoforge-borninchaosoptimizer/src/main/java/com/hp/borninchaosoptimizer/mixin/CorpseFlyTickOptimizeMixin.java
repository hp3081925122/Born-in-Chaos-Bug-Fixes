package com.hp.borninchaosoptimizer.mixin;

import com.hp.borninchaosoptimizer.config.OptimizerConfig;
import com.hp.borninchaosoptimizer.util.EntityEffectHelper;
import net.mcreator.borninchaosv.procedures.CorpseFlyappearanceProcedure;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 优化尸蝇与血虻每刻缓降效果维持逻辑：仅在剩余时长 <= 5 刻时补齐至 25 刻，
 * 避免数百只尸蝇每刻重复分配 MobEffectInstance 并向全服广播数千个效果同步包。
 */
@Mixin(value = CorpseFlyappearanceProcedure.class, remap = false)
public abstract class CorpseFlyTickOptimizeMixin {
    @Inject(method = "execute", at = @At("HEAD"), cancellable = true)
    private static void bic$throttleSlowFallingRefresh(Entity entity, CallbackInfo ci) {
        if (!OptimizerConfig.isEnabled(OptimizerConfig.OPTIMIZE_SKY_AND_SUNBURN_CHECKS)) {
            return;
        }
        ci.cancel();
        if (entity instanceof LivingEntity living) {
            EntityEffectHelper.refreshPassiveEffect(living, MobEffects.SLOW_FALLING, 25, 2, 5);
        }
    }
}
