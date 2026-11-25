package com.hp.borninchaosoptimizer.mixin;

import com.hp.borninchaosoptimizer.config.OptimizerConfig;
import net.mcreator.borninchaosv.init.BornInChaosV1ModMobEffects;
import net.mcreator.borninchaosv.procedures.DarkSplashPriIstiechieniiEffiektaProcedure;
import net.mcreator.borninchaosv.procedures.SweetMadnessPriIstiechieniiEffiektaProcedure;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 修复诅咒印记与亡灵召唤效果在被图腾或指令主动移除时错误触发到期惩罚召唤的缺陷。
 */
@Mixin(value = BornInChaosV1ModMobEffects.class, remap = false)
public abstract class MobEffectsPrematureTriggerMixin {
    @Inject(method = "onEffectRemoved", at = @At("HEAD"), cancellable = true)
    private static void bic$onlyCleanStateOnManualRemove(MobEffectEvent.Remove event, CallbackInfo ci) {
        if (!OptimizerConfig.isEnabled(OptimizerConfig.FIX_EFFECT_REMOVAL_PREMATURE_TRIGGER)) {
            return;
        }
        ci.cancel();
        if (event == null || event.isCanceled()) {
            return;
        }
        MobEffectInstance effectInstance = event.getEffectInstance();
        Entity entity = event.getEntity();
        if (effectInstance == null || entity == null) {
            return;
        }
        // 主动驱散（如图腾净化或指令清除）仅清理内部计数标记，不触发到期惩罚刷怪
        if (effectInstance.getEffect().is(BornInChaosV1ModMobEffects.DARK_SPLASH)) {
            DarkSplashPriIstiechieniiEffiektaProcedure.execute(entity);
        } else if (effectInstance.getEffect().is(BornInChaosV1ModMobEffects.SWEET_MADNESS)) {
            SweetMadnessPriIstiechieniiEffiektaProcedure.execute(entity);
        }
    }
}
