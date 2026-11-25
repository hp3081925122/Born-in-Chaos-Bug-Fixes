package com.hp.borninchaosoptimizer.mixin.core;

import com.hp.borninchaosoptimizer.config.BicOptimizerConfig;
import net.mcreator.borninchaosv.network.BornInChaosV1ModVariables;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.player.PlayerEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 修复 PlayerEvent.Clone 中误调用 Entity.revive() 导致已死亡移除的旧玩家 RemovalReason 被清空为 null 的状态污染问题，
 * 改用标准的 reviveCaps() 并在复制完成后 invalidateCaps()；同时消除登录/重生/切维度同步中的 .orElse(new PlayerVariables()) 临时对象分配。
 */
@Mixin(value = BornInChaosV1ModVariables.EventBusVariableHandlers.class, remap = false)
public abstract class PlayerVariablesEventsMixin {
    @Inject(method = "onPlayerLoggedInSyncPlayerVariables", at = @At("HEAD"), cancellable = true)
    private static void bic$safeSyncOnLogin(PlayerEvent.PlayerLoggedInEvent event, CallbackInfo ci) {
        if (!BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.enableGlobalFixes)) {
            return;
        }
        ci.cancel();
        Player player = event.getEntity();
        if (player != null && !player.level().isClientSide()) {
            player.getCapability(BornInChaosV1ModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                    .ifPresent(vars -> vars.syncPlayerVariables(player));
        }
    }

    @Inject(method = "onPlayerRespawnedSyncPlayerVariables", at = @At("HEAD"), cancellable = true)
    private static void bic$safeSyncOnRespawn(PlayerEvent.PlayerRespawnEvent event, CallbackInfo ci) {
        if (!BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.enableGlobalFixes)) {
            return;
        }
        ci.cancel();
        Player player = event.getEntity();
        if (player != null && !player.level().isClientSide()) {
            player.getCapability(BornInChaosV1ModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                    .ifPresent(vars -> vars.syncPlayerVariables(player));
        }
    }

    @Inject(method = "onPlayerChangedDimensionSyncPlayerVariables", at = @At("HEAD"), cancellable = true)
    private static void bic$safeSyncOnChangeDim(PlayerEvent.PlayerChangedDimensionEvent event, CallbackInfo ci) {
        if (!BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.enableGlobalFixes)) {
            return;
        }
        ci.cancel();
        Player player = event.getEntity();
        if (player != null && !player.level().isClientSide()) {
            player.getCapability(BornInChaosV1ModVariables.PLAYER_VARIABLES_CAPABILITY, null)
                    .ifPresent(vars -> vars.syncPlayerVariables(player));
        }
    }

    @Inject(method = "clonePlayer", at = @At("HEAD"), cancellable = true)
    private static void bic$safeClonePlayer(PlayerEvent.Clone event, CallbackInfo ci) {
        if (!BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.enableGlobalFixes)) {
            return;
        }
        ci.cancel();
        if (event.isWasDeath()) {
            return;
        }
        Player originalPlayer = event.getOriginal();
        Player newPlayer = event.getEntity();
        if (originalPlayer == null || newPlayer == null) {
            return;
        }
        originalPlayer.reviveCaps();
        try {
            originalPlayer.getCapability(BornInChaosV1ModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(origVars ->
                    newPlayer.getCapability(BornInChaosV1ModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(cloneVars ->
                            cloneVars.naughtiness = origVars.naughtiness
                    )
            );
        } finally {
            originalPlayer.invalidateCaps();
        }
    }
}
