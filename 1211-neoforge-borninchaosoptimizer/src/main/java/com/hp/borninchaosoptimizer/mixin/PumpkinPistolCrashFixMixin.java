package com.hp.borninchaosoptimizer.mixin;

import com.hp.borninchaosoptimizer.config.OptimizerConfig;
import com.hp.borninchaosoptimizer.util.Batch5ProcedureHelper;
import net.mcreator.borninchaosv.procedures.PumpkinPistolshotProcedure;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 修复发射南瓜手枪时匿名内部类加载客户端 Minecraft 类导致专用服务端崩溃的问题，并补全武器耐久扣减上下文。
 */
@Mixin(value = PumpkinPistolshotProcedure.class, remap = false)
public abstract class PumpkinPistolCrashFixMixin {
    @Inject(method = "execute", at = @At("HEAD"), cancellable = true)
    private static void bic$fixDedicatedServerCrash(LevelAccessor world, double x, double y, double z, Entity entity, ItemStack itemstack, CallbackInfo ci) {
        if (!OptimizerConfig.isEnabled(OptimizerConfig.FIX_DEDICATED_SERVER_CLIENT_CRASH)) {
            return;
        }
        ci.cancel();
        Batch5ProcedureHelper.executePumpkinPistolShot(world, x, y, z, entity, itemstack);
    }
}
