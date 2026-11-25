package com.hp.borninchaosoptimizer.mixin;

import com.hp.borninchaosoptimizer.config.OptimizerConfig;
import net.mcreator.borninchaosv.entity.MrPumpkinEntity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 修复南瓜先生匿名冲刺 AI Goal 在服务端奇数刻目标变为空时调用 getBoundingBox() 引发的空指针崩服问题。
 */
@Mixin(targets = "net.mcreator.borninchaosv.entity.MrPumpkinEntity$1")
public abstract class MrPumpkinGoalNullTargetFixMixin {
    @Shadow(remap = false)
    @Final
    MrPumpkinEntity this$0;

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void bic$guardNullTargetOnOddTick(CallbackInfo ci) {
        if (!OptimizerConfig.isEnabled(OptimizerConfig.FIX_CUSTOM_GOAL_NULL_TARGET)) {
            return;
        }
        LivingEntity target = this.this$0.getTarget();
        if (target == null || !target.isAlive()) {
            ci.cancel();
        }
    }
}
