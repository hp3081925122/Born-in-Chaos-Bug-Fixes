package com.hp.borninchaosoptimizer.mixin.procedure;

import com.hp.borninchaosoptimizer.config.BicOptimizerConfig;
import net.mcreator.borninchaosv.init.BornInChaosV1ModGameRules;
import net.mcreator.borninchaosv.init.BornInChaosV1ModParticleTypes;
import net.mcreator.borninchaosv.procedures.SpiritGonProcedure;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 优化幽灵类生物日光消散检测：客户端直接跳过，先判定白天与晴天再按间隔检测天空可见性。
 */
@Mixin(value = SpiritGonProcedure.class, remap = false)
public abstract class SpiritSunTickMixin {
    @Inject(method = "execute", at = @At("HEAD"), cancellable = true)
    private static void bicOpt$optimizeSpiritSunTick(LevelAccessor world, double x, double y, double z, Entity entity, CallbackInfo ci) {
        if (!BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.enableGlobalFixes)
                || !BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.optimizeSunAndSkyTickProcedures)) {
            return;
        }
        ci.cancel();
        if (entity == null || entity.isRemoved() || world.isClientSide() || !(world instanceof ServerLevel serverLevel)) {
            return;
        }
        if (!serverLevel.isDay()
                || serverLevel.getLevelData().isRaining()
                || serverLevel.getLevelData().isThundering()) {
            return;
        }
        int interval = Math.max(1, BicOptimizerConfig.getInt(BicOptimizerConfig.COMMON.sunCheckInterval));
        if ((entity.tickCount + entity.getId()) % interval != 0) {
            return;
        }
        if (serverLevel.getGameRules().getBoolean(BornInChaosV1ModGameRules.DISAPPEARANCEOFSPIRITSUNDERTHESUN)
                && serverLevel.canSeeSkyFromBelowWater(BlockPos.containing(x, y + 1.0, z))) {
            entity.discard();
            serverLevel.sendParticles(BornInChaosV1ModParticleTypes.DARK_SMOKE.get(), x, y, z, 10, 0.3, 0.3, 0.3, 0.1);
        }
    }
}
