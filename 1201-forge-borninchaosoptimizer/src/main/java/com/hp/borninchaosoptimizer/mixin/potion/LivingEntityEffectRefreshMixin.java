package com.hp.borninchaosoptimizer.mixin.potion;

import com.hp.borninchaosoptimizer.util.EffectRenewalGuard;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 在原版 LivingEntity.onEffectUpdated 与提前移除的 onEffectRemoved 期间标记上下文，
 * 使自定义药水能够精确区分“自然倒计时归零到期”与“活跃续期 / 图腾牛奶指令提前净化”。
 */
@Mixin(LivingEntity.class)
public abstract class LivingEntityEffectRefreshMixin {
    @Inject(method = "onEffectUpdated", at = @At("HEAD"))
    private void bicOptimizer$enterEffectRefresh(MobEffectInstance effectInstance, boolean reapplyAttributes, Entity source, CallbackInfo ci) {
        if (reapplyAttributes) {
            EffectRenewalGuard.enter();
        }
    }

    @Inject(method = "onEffectUpdated", at = @At("RETURN"))
    private void bicOptimizer$exitEffectRefresh(MobEffectInstance effectInstance, boolean reapplyAttributes, Entity source, CallbackInfo ci) {
        if (reapplyAttributes) {
            EffectRenewalGuard.exit();
        }
    }

    @Inject(method = "onEffectRemoved", at = @At("HEAD"))
    private void bicOptimizer$enterEffectRemoved(MobEffectInstance effectInstance, CallbackInfo ci) {
        if (effectInstance != null && effectInstance.getDuration() > 0) {
            EffectRenewalGuard.enterEarlyRemoval();
        }
    }

    @Inject(method = "onEffectRemoved", at = @At("RETURN"))
    private void bicOptimizer$exitEffectRemoved(MobEffectInstance effectInstance, CallbackInfo ci) {
        if (effectInstance != null && effectInstance.getDuration() > 0) {
            EffectRenewalGuard.exitEarlyRemoval();
        }
    }
}
