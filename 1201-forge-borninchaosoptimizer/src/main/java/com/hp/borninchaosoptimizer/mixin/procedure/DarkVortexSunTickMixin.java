package com.hp.borninchaosoptimizer.mixin.procedure;

import com.hp.borninchaosoptimizer.config.BicOptimizerConfig;
import net.mcreator.borninchaosv.init.BornInChaosV1ModGameRules;
import net.mcreator.borninchaosv.init.BornInChaosV1ModParticleTypes;
import net.mcreator.borninchaosv.procedures.DarcGonProcedure;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 优化暗影漩涡日光消散检测：客户端直接跳过，夜晚与雨天短路天空光照扫描，完整保留每 tick 暗影微粒。
 */
@Mixin(value = DarcGonProcedure.class, remap = false)
public abstract class DarkVortexSunTickMixin {
    @Inject(method = "execute", at = @At("HEAD"), cancellable = true)
    private static void bicOpt$optimizeDarkVortexTick(LevelAccessor world, double x, double y, double z, Entity entity, CallbackInfo ci) {
        if (!BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.enableGlobalFixes)
                || !BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.optimizeSunAndSkyTickProcedures)) {
            return;
        }
        ci.cancel();
        if (entity == null || world.isClientSide() || !(world instanceof ServerLevel serverLevel)) {
            return;
        }
        int interval = Math.max(1, BicOptimizerConfig.getInt(BicOptimizerConfig.COMMON.sunCheckInterval));
        if (!entity.isRemoved()
                && serverLevel.isDay()
                && !serverLevel.getLevelData().isRaining()
                && !serverLevel.getLevelData().isThundering()
                && (entity.tickCount + entity.getId()) % interval == 0
                && serverLevel.getGameRules().getBoolean(BornInChaosV1ModGameRules.DISAPPEARANCEOFSPIRITSUNDERTHESUN)
                && serverLevel.canSeeSkyFromBelowWater(BlockPos.containing(x, y + 1.0, z))) {
            entity.discard();
            serverLevel.sendParticles(BornInChaosV1ModParticleTypes.DARK_SMOKE.get(), x, y, z, 10, 0.3, 0.3, 0.3, 0.1);
        }
        serverLevel.sendParticles(BornInChaosV1ModParticleTypes.DIM.get(), x, y, z, 1, 0.2, 0.2, 0.2, 0.1);
    }
}
