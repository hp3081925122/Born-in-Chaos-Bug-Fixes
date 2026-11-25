package com.hp.borninchaosoptimizer.mixin;

import com.hp.borninchaosoptimizer.config.OptimizerConfig;
import net.mcreator.borninchaosv.entity.BloodyGadflyEntity;
import net.mcreator.borninchaosv.entity.CorpseFlyEntity;
import net.mcreator.borninchaosv.procedures.SpinyShellTrapPriStolknovieniiSushchnostiSBlokomProcedure;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 修复刺壳陷阱碰撞过程将 !(entity instanceof CorpseFlyEntity) && !(entity instanceof BloodyGadflyEntity) 误写为逻辑或（恒真式），导致飞虫无法按设计免疫地面刺壳陷阱的缺陷。
 */
@Mixin(value = SpinyShellTrapPriStolknovieniiSushchnostiSBlokomProcedure.class, remap = false)
public abstract class SpinyShellTrapCollisionFixMixin {
    @Inject(method = "execute", at = @At("HEAD"), cancellable = true)
    private static void bic$fixFlyingInsectImmunityTautology(LevelAccessor world, Entity entity, CallbackInfo ci) {
        if (OptimizerConfig.isEnabled(OptimizerConfig.FIX_TRAP_AND_AOE_NON_LIVING_DAMAGE)
                && (entity instanceof CorpseFlyEntity || entity instanceof BloodyGadflyEntity)) {
            ci.cancel();
        }
    }
}
