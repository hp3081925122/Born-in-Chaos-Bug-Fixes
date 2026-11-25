package com.hp.borninchaosoptimizer.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.loading.FMLEnvironment;

/**
 * 专用服务端安全的客户端游戏模式查询与图腾激活渲染桥接工具。
 * 通过物理端判断与独立静态内部类隔离，保证专用服务端绝不触发客户端类加载。
 */
public final class ClientTotemDisplayHelper {
    private ClientTotemDisplayHelper() {
    }

    /**
     * 双端安全判定玩家是否处于指定游戏模式，修复原模组匿名内部类加载客户端类及连接为空时的空指针问题。
     */
    public static boolean isGameMode(Entity entity, GameType expected) {
        if (entity instanceof ServerPlayer serverPlayer) {
            return serverPlayer.gameMode.getGameModeForPlayer() == expected;
        }
        if (entity instanceof Player player && entity.level().isClientSide() && FMLEnvironment.dist == Dist.CLIENT) {
            return ClientOnly.isClientPlayerGameMode(player, expected);
        }
        return false;
    }

    /**
     * 仅在物理客户端安全触发屏幕图腾浮现动画，避免专用服务端抛出类缺失崩溃。
     */
    public static void displayActivation(ItemStack stack) {
        if (FMLEnvironment.dist == Dist.CLIENT) {
            ClientOnly.displayItemActivation(stack);
        }
    }

    private static final class ClientOnly {
        private static boolean isClientPlayerGameMode(Player player, GameType expected) {
            Minecraft mc = Minecraft.getInstance();
            if (mc == null) {
                return false;
            }
            ClientPacketListener connection = mc.getConnection();
            if (connection == null) {
                return false;
            }
            PlayerInfo info = connection.getPlayerInfo(player.getGameProfile().getId());
            return info != null && info.getGameMode() == expected;
        }

        private static void displayItemActivation(ItemStack stack) {
            Minecraft mc = Minecraft.getInstance();
            if (mc != null && mc.gameRenderer != null) {
                mc.gameRenderer.displayItemActivation(stack);
            }
        }
    }
}
