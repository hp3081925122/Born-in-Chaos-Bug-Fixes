package com.hp.borninchaosoptimizer.mixin.potion;

import com.hp.borninchaosoptimizer.config.BicOptimizerConfig;
import com.hp.borninchaosoptimizer.util.EffectRenewalGuard;
import net.mcreator.borninchaosv.potion.CursedMarkMobEffect;
import net.mcreator.borninchaosv.potion.DarkSplashMobEffect;
import net.mcreator.borninchaosv.potion.SweetMadnessMobEffect;
import net.mcreator.borninchaosv.potion.UndeadSummonunMobEffect;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 拦截诅咒印记、亡灵召唤、暗影溅射与甜蜜疯狂在活跃续期（onEffectUpdated）或提前净化（不死图腾/牛奶/指令清除）期间误触发的到期逻辑，
 * 避免刷新印记或触发图腾净化时反而提前召唤 4 只猩红追猎者，并防止续杯时提前清空暗影溅射蓄力与甜蜜疯狂已食用糖果记录，同时完整保留自然到期行为。
 */
@Mixin(value = {
        CursedMarkMobEffect.class,
        UndeadSummonunMobEffect.class,
        DarkSplashMobEffect.class,
        SweetMadnessMobEffect.class
})
public abstract class MobEffectExpiryGuardMixin {
    @Inject(
            method = "removeAttributeModifiers",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/effect/MobEffect;removeAttributeModifiers(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/ai/attributes/AttributeMap;I)V",
                    shift = At.Shift.AFTER
            ),
            cancellable = true
    )
    private void bicOptimizer$skipExpiryOnRefreshOrEarlyCure(LivingEntity entity, AttributeMap attributeMap, int amplifier, CallbackInfo ci) {
        if (!BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.enableGlobalFixes)) {
            return;
        }
        if (EffectRenewalGuard.isRefreshing()
                && BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.fixPotionRefreshPrematureExpiry)) {
            ci.cancel();
            return;
        }
        if (EffectRenewalGuard.isEarlyRemoval()
                && !((Object) this instanceof DarkSplashMobEffect)
                && !((Object) this instanceof SweetMadnessMobEffect)
                && BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.fixPrematureCureExpiry)) {
            ci.cancel();
        }
    }
}
