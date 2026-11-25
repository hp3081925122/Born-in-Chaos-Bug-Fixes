package com.hp.borninchaosoptimizer.util;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;

/**
 * 客户端渲染与玩家游戏模式安全隔离桥，防止专用服务端在类加载与字节码校验阶段触碰客户端类。
 */
public final class SafeClientBridge {
    private SafeClientBridge() {
    }

    public static void displayItemActivation(ItemStack stack) {
        if (FMLEnvironment.dist == Dist.CLIENT) {
            ClientOnly.display(stack);
        }
    }

    /**
     * 安全判定实体是否为生存模式玩家，服务端走 ServerPlayerGameMode，客户端走 PlayerInfo。
     */
    public static boolean isSurvival(Entity entity) {
        if (entity instanceof ServerPlayer serverPlayer) {
            return serverPlayer.gameMode.getGameModeForPlayer() == GameType.SURVIVAL;
        }
        if (entity != null && entity.level().isClientSide() && entity instanceof Player player && FMLEnvironment.dist == Dist.CLIENT) {
            return ClientOnly.hasGameMode(player, GameType.SURVIVAL);
        }
        return false;
    }

    /**
     * 安全判定实体是否为创造模式玩家，避免原模组匿名内部类直接引用 Minecraft 客户端单例。
     */
    public static boolean isCreative(Entity entity) {
        if (entity instanceof ServerPlayer serverPlayer) {
            return serverPlayer.gameMode.getGameModeForPlayer() == GameType.CREATIVE;
        }
        if (entity instanceof Player player) {
            if (player.getAbilities().instabuild) {
                return true;
            }
            if (entity.level().isClientSide() && FMLEnvironment.dist == Dist.CLIENT) {
                return ClientOnly.hasGameMode(player, GameType.CREATIVE);
            }
        }
        return false;
    }

    private static final class ClientOnly {
        private static void display(ItemStack stack) {
            net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
            if (mc != null && mc.gameRenderer != null) {
                mc.gameRenderer.displayItemActivation(stack);
            }
        }

        private static boolean hasGameMode(Player player, GameType expected) {
            net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
            if (mc != null && mc.getConnection() != null) {
                net.minecraft.client.multiplayer.PlayerInfo info = mc.getConnection().getPlayerInfo(player.getGameProfile().getId());
                return info != null && info.getGameMode() == expected;
            }
            return false;
        }
    }
}
