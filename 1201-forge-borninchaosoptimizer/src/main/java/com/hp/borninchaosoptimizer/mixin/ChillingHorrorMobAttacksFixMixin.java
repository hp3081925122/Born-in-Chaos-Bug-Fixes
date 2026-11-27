package com.hp.borninchaosoptimizer.mixin;

import net.mcreator.borninchaosv.procedures.ChillingHorrorMobAttacksProcedure;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

// 修复 MCR-53：防止 ChillingHorrorMobAttacksProcedure 在 entity.teleportTo(y - 0.3) 后再次以已变化坐标调用 connection.teleport 导致玩家被双倍压入地下 0.6 格
@Mixin(value = ChillingHorrorMobAttacksProcedure.class, remap = false)
public abstract class ChillingHorrorMobAttacksFixMixin {
    @Redirect(
            method = "execute(Lnet/minecraftforge/eventbus/api/Event;Lnet/minecraft/world/level/LevelAccessor;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/entity/Entity;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/network/ServerGamePacketListenerImpl;teleport(DDDFF)V",
                    remap = true
            )
    )
    private static void bicOpt$syncExistingCoords(ServerGamePacketListenerImpl connection, double x, double y, double z, float yRot, float xRot) {
        ServerPlayer player = connection.player;
        connection.teleport(player.getX(), player.getY(), player.getZ(), yRot, xRot);
    }
}
