package com.hp.borninchaosoptimizer.mixin;

import com.hp.borninchaosoptimizer.config.OptimizerConfig;
import net.mcreator.borninchaosv.entity.KrampusHenchmanEntity;
import net.mcreator.borninchaosv.init.BornInChaosV1ModMobEffects;
import net.mcreator.borninchaosv.init.BornInChaosV1ModParticleTypes;
import net.mcreator.borninchaosv.init.BornInChaosV1ModSounds;
import net.mcreator.borninchaosv.procedures.KrampusHenchmanAttackProcedure;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.neoforged.bus.api.Event;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 修复坎卜斯爪牙击落单件物品时无意义遍历深拷贝整包 41 格物品栈的问题。
 */
@Mixin(value = KrampusHenchmanAttackProcedure.class, remap = false)
public abstract class KrampusHenchmanAttackFixMixin {
    @Inject(method = "execute(Lnet/neoforged/bus/api/Event;Lnet/minecraft/world/level/LevelAccessor;DDDLnet/minecraft/world/entity/Entity;Lnet/minecraft/world/entity/Entity;)V", at = @At("HEAD"), cancellable = true)
    private static void bic$fixHenchmanDisarm(Event event, LevelAccessor world, double x, double y, double z, Entity entity, Entity sourceentity, CallbackInfo ci) {
        if (!OptimizerConfig.isEnabled(OptimizerConfig.FIX_KRAMPUS_DISARM_AND_SHIELD)) {
            return;
        }
        ci.cancel();
        if (!(sourceentity instanceof KrampusHenchmanEntity) || !(entity instanceof Player player)) {
            return;
        }
        if (player.isBlocking() || !(Math.random() < 0.35)) {
            return;
        }
        ItemStack mainHand = player.getMainHandItem();
        if (!mainHand.isEmpty() && mainHand.getMaxStackSize() == 1) {
            if (!world.isClientSide() && world instanceof Level level) {
                level.playSound(null, BlockPos.containing(x, y, z), BornInChaosV1ModSounds.MINION_BLOW.get(), SoundSource.NEUTRAL, 1.0f, 1.0f);
            }
            if (world instanceof ServerLevel serverLevel) {
                ItemEntity dropped = new ItemEntity(serverLevel, player.getX(), player.getY() + 1.0, player.getZ(), mainHand.copy());
                dropped.setPickUpDelay(45);
                serverLevel.addFreshEntity(dropped);
                player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
                serverLevel.sendParticles(BornInChaosV1ModParticleTypes.STUNSTARS.get(), player.getX(), player.getY() + 1.3, player.getZ(), 3, 0.25, 0.3, 0.25, 0.1);
            }
            if (!player.level().isClientSide()) {
                player.addEffect(new MobEffectInstance(BornInChaosV1ModMobEffects.LOST_ITEMS, 35, 0, false, false));
            }
        }
    }
}
