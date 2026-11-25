package com.hp.borninchaosoptimizer.mixin;

import com.hp.borninchaosoptimizer.config.OptimizerConfig;
import net.mcreator.borninchaosv.init.BornInChaosV1ModGameRules;
import net.mcreator.borninchaosv.init.BornInChaosV1ModParticleTypes;
import net.mcreator.borninchaosv.procedures.DarcGonProcedure;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 优化暗影漩涡每刻过程：先检查服务端、晴朗白天与游戏规则，再调用 canSeeSkyFromBelowWater，消除夜晚与客户端每刻的天空扫描。
 */
@Mixin(value = DarcGonProcedure.class, remap = false)
public abstract class DarkVortexTickOptimizeMixin {
    @Inject(method = "execute", at = @At("HEAD"), cancellable = true)
    private static void bic$optimizeDarkVortexTick(LevelAccessor world, double x, double y, double z, Entity entity, CallbackInfo ci) {
        if (!OptimizerConfig.isEnabled(OptimizerConfig.OPTIMIZE_SKY_AND_SUNBURN_CHECKS)) {
            return;
        }
        ci.cancel();
        if (entity == null || !(world instanceof ServerLevel serverLevel)) {
            return;
        }
        Level level = serverLevel;
        if (level.isDay()
                && !level.isRaining()
                && !level.isThundering()
                && level.getGameRules().getBoolean(BornInChaosV1ModGameRules.DISAPPEARANCEOFSPIRITSUNDERTHESUN)
                && world.canSeeSkyFromBelowWater(BlockPos.containing(x, y + 1.0, z))) {
            entity.discard();
            serverLevel.sendParticles(BornInChaosV1ModParticleTypes.DARK_SMOKE.get(), x, y, z, 10, 0.3, 0.3, 0.3, 0.1);
        }
        serverLevel.sendParticles(BornInChaosV1ModParticleTypes.DIM.get(), x, y, z, 1, 0.2, 0.2, 0.2, 0.1);
    }
}
