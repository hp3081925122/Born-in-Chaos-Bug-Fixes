package com.hp.borninchaosoptimizer.mixin.combat;

import com.hp.borninchaosoptimizer.config.BicOptimizerConfig;
import com.hp.borninchaosoptimizer.util.RetaliationGuard;
import net.mcreator.borninchaosv.procedures.InsectProtectionTrigerProcedure;
import net.mcreator.borninchaosv.procedures.LordPumpkinheadsHatHitProcedure;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 为防虫药水与南瓜领主帽反伤监听添加服务端限定与防重入锁，防止互相反伤导致无限递归崩溃。
 */
@Mixin(value = {
        InsectProtectionTrigerProcedure.class,
        LordPumpkinheadsHatHitProcedure.class
}, remap = false)
public abstract class GuardedRetaliationProceduresMixin {
    @Inject(method = "onEntityAttacked", at = @At("HEAD"), cancellable = true)
    private static void bic$enterRetaliation(LivingAttackEvent event, CallbackInfo ci) {
        if (!BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.enableGlobalFixes)
                || !BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.fixGlobalCombatEvents)) {
            return;
        }
        if (event == null || event.getEntity() == null || event.getEntity().level().isClientSide()) {
            ci.cancel();
            return;
        }
        if (!RetaliationGuard.tryEnter()) {
            ci.cancel();
        }
    }

    @Inject(method = "onEntityAttacked", at = @At("RETURN"))
    private static void bic$exitRetaliation(LivingAttackEvent event, CallbackInfo ci) {
        if (!ci.isCancelled()
                && BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.enableGlobalFixes)
                && BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.fixGlobalCombatEvents)) {
            RetaliationGuard.exit();
        }
    }
}
