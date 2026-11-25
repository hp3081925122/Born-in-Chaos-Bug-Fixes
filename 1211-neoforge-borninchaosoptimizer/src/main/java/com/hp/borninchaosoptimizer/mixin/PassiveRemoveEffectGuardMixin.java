package com.hp.borninchaosoptimizer.mixin;

import com.hp.borninchaosoptimizer.config.OptimizerConfig;
import net.mcreator.borninchaosv.procedures.BabySpiderControlledPriObnovlieniiTikaSushchnostiProcedure;
import net.mcreator.borninchaosv.procedures.KrampusHenchmanPriObnovlieniiTikaSushchnostiProcedure;
import net.mcreator.borninchaosv.procedures.KrampusPriObnovlieniiTikaSushchnostiProcedure;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * 拦截受控幼蛛、坎卜斯与坎卜斯爪牙每刻免疫毒/寒骨效果时无条件调用的 removeEffect，
 * 仅在确实携带对应效果时移除，消除每刻向 NeoForge 事件总线广播 MobEffectEvent.Remove 的开销。
 */
@Mixin(value = {
        BabySpiderControlledPriObnovlieniiTikaSushchnostiProcedure.class,
        KrampusHenchmanPriObnovlieniiTikaSushchnostiProcedure.class,
        KrampusPriObnovlieniiTikaSushchnostiProcedure.class
}, remap = false)
public abstract class PassiveRemoveEffectGuardMixin {
    @Redirect(
            method = "execute",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/LivingEntity;removeEffect(Lnet/minecraft/core/Holder;)Z"
            )
    )
    private static boolean bic$guardRemoveEffect(LivingEntity entity, Holder<MobEffect> effect) {
        if (!OptimizerConfig.isEnabled(OptimizerConfig.OPTIMIZE_SKY_AND_SUNBURN_CHECKS)) {
            return entity.removeEffect(effect);
        }
        if (entity.hasEffect(effect)) {
            return entity.removeEffect(effect);
        }
        return false;
    }
}
