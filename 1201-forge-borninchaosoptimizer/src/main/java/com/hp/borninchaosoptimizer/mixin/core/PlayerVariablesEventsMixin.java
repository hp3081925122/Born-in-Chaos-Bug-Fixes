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
 * 改用标准的 reviveCaps() 并在复制完成后 invalidateCaps()。
 */
@Mixin(value = BornInChaosV1ModVariables.EventBusVariableHandlers.class, remap = false)
public abstract class PlayerVariablesEventsMixin {
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
