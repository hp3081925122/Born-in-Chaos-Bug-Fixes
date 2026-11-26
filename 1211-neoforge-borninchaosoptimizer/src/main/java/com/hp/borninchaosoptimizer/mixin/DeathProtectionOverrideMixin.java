package com.hp.borninchaosoptimizer.mixin;

import com.hp.borninchaosoptimizer.config.OptimizerConfig;
import net.mcreator.borninchaosv.procedures.DarkWardActivProcedure;
import net.mcreator.borninchaosv.procedures.DeathTotemKoghdaPriedmietNakhoditsiaVRukieProcedure;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 当已启用 HIGH 优先级的 BicDeathProtectionHandler 统一接管暗影守护与死亡图腾时，
 * 屏蔽原模组在 NORMAL 优先级下的重复 LivingDeathEvent 处理逻辑。
 */
@Mixin(value = {
        DarkWardActivProcedure.class,
        DeathTotemKoghdaPriedmietNakhoditsiaVRukieProcedure.class
}, remap = false)
public abstract class DeathProtectionOverrideMixin {
    @Inject(method = "onEntityDeath", at = @At("HEAD"), cancellable = true)
    private static void bic$skipLegacyDeathListener(LivingDeathEvent event, CallbackInfo ci) {
        if (OptimizerConfig.isEnabled(OptimizerConfig.FIX_DEATH_PROTECTION_EVENTS)) {
            ci.cancel();
        }
    }
}
