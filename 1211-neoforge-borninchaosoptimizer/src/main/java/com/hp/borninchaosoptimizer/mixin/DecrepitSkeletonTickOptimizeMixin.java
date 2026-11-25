package com.hp.borninchaosoptimizer.mixin;

import com.hp.borninchaosoptimizer.config.OptimizerConfig;
import com.hp.borninchaosoptimizer.util.EntityEffectHelper;
import net.mcreator.borninchaosv.procedures.DecrepitSkeletonPriObnovlieniiTaktaSushchnostiProcedure;
import net.mcreator.borninchaosv.procedures.RestlessSpiritPriObnovlieniiTaktaSushchnostiProcedure;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 优化十余种亡灵生物共用的阳光自燃每刻过程：将客户端、夜晚、雨天与已着火检查前置，消除全天候双端逐格向上扫描天空视线的开销。
 */
@Mixin(value = {
        DecrepitSkeletonPriObnovlieniiTaktaSushchnostiProcedure.class,
        RestlessSpiritPriObnovlieniiTaktaSushchnostiProcedure.class
}, remap = false)
public abstract class DecrepitSkeletonTickOptimizeMixin {
    @Inject(method = "execute", at = @At("HEAD"), cancellable = true)
    private static void bic$fastSunburnCheck(LevelAccessor world, double x, double y, double z, Entity entity, CallbackInfo ci) {
        if (!OptimizerConfig.isEnabled(OptimizerConfig.OPTIMIZE_SKY_AND_SUNBURN_CHECKS)) {
            return;
        }
        ci.cancel();
        if (entity != null) {
            EntityEffectHelper.handleUndeadSunburn(world, x, y, z, entity);
        }
    }
}
