package com.hp.borninchaosoptimizer.mixin;

import com.hp.borninchaosoptimizer.config.OptimizerConfig;
import com.hp.borninchaosoptimizer.util.Batch5ProcedureHelper;
import net.mcreator.borninchaosv.procedures.RottenSoilPriRazrushieniiBlokaIghrokomProcedure;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 修复挖掘腐壤时匿名内部类直接引用客户端 Minecraft 单例导致专用服务端崩溃的问题。
 */
@Mixin(value = RottenSoilPriRazrushieniiBlokaIghrokomProcedure.class, remap = false)
public abstract class RottenSoilCrashFixMixin {
    @Inject(method = "execute", at = @At("HEAD"), cancellable = true)
    private static void bic$fixDedicatedServerCrash(LevelAccessor world, double x, double y, double z, Entity entity, CallbackInfo ci) {
        if (!OptimizerConfig.isEnabled(OptimizerConfig.FIX_DEDICATED_SERVER_CLIENT_CRASH)) {
            return;
        }
        ci.cancel();
        Batch5ProcedureHelper.executeRottenSoilBroken(world, x, y, z, entity);
    }
}
