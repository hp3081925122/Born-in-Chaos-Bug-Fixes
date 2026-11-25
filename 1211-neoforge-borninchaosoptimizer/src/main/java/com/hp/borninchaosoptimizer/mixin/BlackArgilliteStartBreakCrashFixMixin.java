package com.hp.borninchaosoptimizer.mixin;

import com.hp.borninchaosoptimizer.config.OptimizerConfig;
import com.hp.borninchaosoptimizer.util.SafeClientBridge;
import net.mcreator.borninchaosv.init.BornInChaosV1ModItems;
import net.mcreator.borninchaosv.procedures.BlackArgilliteNKoghdaIghrokNachinaietUnichtozhatProcedure;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.neoforged.neoforge.common.ItemAbilities;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 修复左键开始挖掘黑泥岩时专用服务端加载客户端渲染类导致崩溃的问题，并兼容标准镐标签与工具能力。
 */
@Mixin(value = BlackArgilliteNKoghdaIghrokNachinaietUnichtozhatProcedure.class, remap = false)
public abstract class BlackArgilliteStartBreakCrashFixMixin {
    @Inject(method = "execute", at = @At("HEAD"), cancellable = true)
    private static void bic$safeServerStartBreak(LevelAccessor world, double x, double y, double z, Entity entity, CallbackInfo ci) {
        if (!OptimizerConfig.isEnabled(OptimizerConfig.FIX_DEDICATED_SERVER_CLIENT_CRASH)) {
            return;
        }
        ci.cancel();
        if (!(entity instanceof Player player) || !bic$isSurvivalPlayer(player)) {
            return;
        }
        if (player.hasEffect(MobEffects.DIG_SLOWDOWN)) {
            return;
        }
        ItemStack mainHand = player.getMainHandItem();
        boolean isPickaxe = mainHand.getItem() instanceof PickaxeItem
                || (OptimizerConfig.isEnabled(OptimizerConfig.FIX_HARDCODED_TOOL_CHECKS)
                        && (mainHand.is(ItemTags.PICKAXES) || mainHand.canPerformAction(ItemAbilities.PICKAXE_DIG)));
        if (!isPickaxe) {
            return;
        }
        if (world.isClientSide()) {
            SafeClientBridge.displayItemActivation(new ItemStack(BornInChaosV1ModItems.AI.get()));
        }
        if (!player.level().isClientSide()) {
            player.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 1000, 2));
        }
        if (world instanceof Level level) {
            if (!level.isClientSide()) {
                level.playSound(null, BlockPos.containing(x, y, z), SoundEvents.ELDER_GUARDIAN_CURSE, SoundSource.NEUTRAL, 0.8f, 0.9f);
            } else {
                level.playLocalSound(x, y, z, SoundEvents.ELDER_GUARDIAN_CURSE, SoundSource.NEUTRAL, 0.8f, 0.9f, false);
            }
        }
    }

    @Unique
    private static boolean bic$isSurvivalPlayer(Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            return serverPlayer.gameMode.getGameModeForPlayer() == GameType.SURVIVAL;
        }
        return !player.isCreative() && !player.isSpectator();
    }
}
