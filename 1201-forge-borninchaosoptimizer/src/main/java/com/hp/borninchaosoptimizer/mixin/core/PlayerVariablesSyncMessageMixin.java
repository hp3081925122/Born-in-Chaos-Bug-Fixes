package com.hp.borninchaosoptimizer.mixin.core;

import net.mcreator.borninchaosv.network.BornInChaosV1ModVariables;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraftforge.network.NetworkEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Supplier;

/**
 * 修复登录竞态下客户端收到同步包时 Minecraft.getInstance().player 可能尚未初始化导致的空指针断连。
 */
@Mixin(value = BornInChaosV1ModVariables.PlayerVariablesSyncMessage.class, remap = false)
public abstract class PlayerVariablesSyncMessageMixin {
    @Shadow(remap = false)
    @Final
    private BornInChaosV1ModVariables.PlayerVariables data;

    @Inject(method = "handler", at = @At("HEAD"), cancellable = true)
    private static void bic$safeClientHandler(
            BornInChaosV1ModVariables.PlayerVariablesSyncMessage message,
            Supplier<NetworkEvent.Context> contextSupplier,
            CallbackInfo ci
    ) {
        ci.cancel();
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            if (!context.getDirection().getReceptionSide().isServer()) {
                LocalPlayer clientPlayer = Minecraft.getInstance().player;
                if (clientPlayer == null || message == null) {
                    return;
                }
                BornInChaosV1ModVariables.PlayerVariables incoming = ((PlayerVariablesSyncMessageMixin) (Object) message).data;
                if (incoming == null) {
                    return;
                }
                clientPlayer.getCapability(BornInChaosV1ModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                        .ifPresent(vars -> vars.naughtiness = incoming.naughtiness);
            }
        });
        context.setPacketHandled(true);
    }
}
