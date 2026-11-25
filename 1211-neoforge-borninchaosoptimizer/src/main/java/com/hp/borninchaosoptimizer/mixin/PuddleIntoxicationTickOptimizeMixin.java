package com.hp.borninchaosoptimizer.mixin;

import com.hp.borninchaosoptimizer.config.OptimizerConfig;
import com.hp.borninchaosoptimizer.util.Batch5ProcedureHelper;
import net.mcreator.borninchaosv.procedures.PuddleofintoxicationObnovlieniieTikaProcedure;
import net.minecraft.world.level.LevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 优化迷醉水洼方块每刻倒计时过程：完整保留服务端烟雾粒子与消失音效，消除持续 400 刻的每刻整块 NBT 发包与客户端区块重建。
 */
@Mixin(value = PuddleofintoxicationObnovlieniieTikaProcedure.class, remap = false)
public abstract class PuddleIntoxicationTickOptimizeMixin {
    @Inject(method = "execute", at = @At("HEAD"), cancellable = true)
    private static void bic$optimizeIntoxicationPuddleTick(LevelAccessor world, double x, double y, double z, CallbackInfo ci) {
        if (!OptimizerConfig.isEnabled(OptimizerConfig.OPTIMIZE_TEMPORARY_BLOCK_ENTITY_SYNC)) {
            return;
        }
        ci.cancel();
        Batch5ProcedureHelper.executePuddleTick(world, x, y, z, true);
    }
}
