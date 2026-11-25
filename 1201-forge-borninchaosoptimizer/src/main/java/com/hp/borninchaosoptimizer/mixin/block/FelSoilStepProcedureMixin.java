package com.hp.borninchaosoptimizer.mixin.block;

import com.hp.borninchaosoptimizer.config.BicOptimizerConfig;
import net.mcreator.borninchaosv.procedures.FelSoilKoghdaSushchnostKhoditPoBlokuProcedure;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 限制邪土踩踏灼烧过程仅在服务端执行，避免客户端每 tick 空构造伤害源。
 */
@Mixin(value = FelSoilKoghdaSushchnostKhoditPoBlokuProcedure.class, remap = false)
public abstract class FelSoilStepProcedureMixin {
    @Inject(method = "execute", at = @At("HEAD"), cancellable = true)
    private static void bicOpt$serverOnly(LevelAccessor world, Entity entity, CallbackInfo ci) {
        if (BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.enableGlobalFixes)
                && BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.optimizeTemporaryTickingBlocks)
                && (entity == null || world.isClientSide())) {
            ci.cancel();
        }
    }
}
