package com.hp.borninchaosoptimizer.mixin;

import net.mcreator.borninchaosv.network.BornInChaosV1ModVariables;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 防止客户端进服加载早期竞态阶段收到玩家变量同步包时因 context.player() 为空抛出异常导致断连。
 */
@Mixin(value = BornInChaosV1ModVariables.PlayerVariablesSyncMessage.class, remap = false)
public abstract class PlayerVariablesSyncSafeMixin {
    @Inject(method = "handleData", at = @At("HEAD"), cancellable = true)
    private static void bic$guardNullContextPlayer(BornInChaosV1ModVariables.PlayerVariablesSyncMessage message, IPayloadContext context, CallbackInfo ci) {
        if (context == null || context.player() == null) {
            ci.cancel();
        }
    }
}
