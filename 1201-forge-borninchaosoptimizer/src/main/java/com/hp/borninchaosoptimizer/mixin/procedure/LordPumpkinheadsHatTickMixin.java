package com.hp.borninchaosoptimizer.mixin.procedure;

import com.hp.borninchaosoptimizer.config.BicOptimizerConfig;
import net.mcreator.borninchaosv.init.BornInChaosV1ModMobEffects;
import net.mcreator.borninchaosv.procedures.LordPumpkinheadsHatKazhdyiTikDliaShliemaProcedure;
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
 * 优化南瓜领主礼帽每 tick 过程：仅服务端执行，并在清除三种负面药水前检查 hasEffect，消除每 tick 三重事件风暴。
 */
@Mixin(value = LordPumpkinheadsHatKazhdyiTikDliaShliemaProcedure.class, remap = false)
public abstract class LordPumpkinheadsHatTickMixin {
    @Inject(method = "execute", at = @At("HEAD"), cancellable = true)
    private static void bicOpt$optimizeHatTick(Entity entity, CallbackInfo ci) {
        if (!BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.enableGlobalFixes)
                || !BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.optimizeInventoryAndArmorTicks)) {
            return;
        }
        ci.cancel();
        if (!(entity instanceof LivingEntity living) || living.level().isClientSide()) {
            return;
        }

        living.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 10, 0, false, false));

        MobEffect infernalFlame = BornInChaosV1ModMobEffects.INFERNAL_FLAME.get();
        if (living.hasEffect(infernalFlame)) {
            living.removeEffect(infernalFlame);
        }
        MobEffect soulStrat = BornInChaosV1ModMobEffects.SOUL_STRATIFICATION.get();
        if (living.hasEffect(soulStrat)) {
            living.removeEffect(soulStrat);
        }
        MobEffect livingBomb = BornInChaosV1ModMobEffects.LIVING_BOMB.get();
        if (living.hasEffect(livingBomb)) {
            living.removeEffect(livingBomb);
        }
    }
}
