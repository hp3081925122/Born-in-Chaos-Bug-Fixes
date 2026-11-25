package com.hp.borninchaosoptimizer.mixin;

import com.hp.borninchaosoptimizer.config.OptimizerConfig;
import net.mcreator.borninchaosv.entity.KrampusEntity;
import net.mcreator.borninchaosv.init.BornInChaosV1ModMobEffects;
import net.mcreator.borninchaosv.init.BornInChaosV1ModParticleTypes;
import net.mcreator.borninchaosv.init.BornInChaosV1ModSounds;
import net.mcreator.borninchaosv.procedures.KrampusAttackProcedure;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.neoforged.bus.api.Event;
import net.neoforged.neoforge.common.ItemAbilities;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 修复坎卜斯攻击时对空手玩家生成空掉落物、遍历深拷贝整包 41 格物品栈以及破盾缺少持有者上下文的问题。
 */
@Mixin(value = KrampusAttackProcedure.class, remap = false)
public abstract class KrampusAttackFixMixin {
    @Inject(method = "execute(Lnet/neoforged/bus/api/Event;Lnet/minecraft/world/level/LevelAccessor;DDDLnet/minecraft/world/entity/Entity;Lnet/minecraft/world/entity/Entity;)V", at = @At("HEAD"), cancellable = true)
    private static void bic$fixKrampusAttack(Event event, LevelAccessor world, double x, double y, double z, Entity entity, Entity sourceentity, CallbackInfo ci) {
        if (!OptimizerConfig.isEnabled(OptimizerConfig.FIX_KRAMPUS_DISARM_AND_SHIELD)) {
            return;
        }
        ci.cancel();
        if (!(sourceentity instanceof KrampusEntity krampus) || !(entity instanceof LivingEntity target)) {
            return;
        }
        if (!(entity instanceof Mob || entity instanceof Player)) {
            return;
        }
        if (!target.isBlocking() && Math.random() < 0.35) {
            if (!world.isClientSide() && world instanceof Level level) {
                level.playSound(null, BlockPos.containing(x, y, z), BornInChaosV1ModSounds.KRAMPUS_BLOW.get(), SoundSource.NEUTRAL, 1.0f, 1.0f);
            }
            if (target.hasEffect(BornInChaosV1ModMobEffects.BONE_CHILLING)) {
                krampus.setHealth(Math.min(krampus.getMaxHealth(), krampus.getHealth() + 8.0f));
            }
            if (target instanceof Player player) {
                ItemStack mainHand = player.getMainHandItem();
                if (!mainHand.isEmpty()) {
                    if (world instanceof ServerLevel serverLevel) {
                        ItemEntity dropped = new ItemEntity(serverLevel, player.getX(), player.getY() + 1.0, player.getZ(), mainHand.copy());
                        dropped.setPickUpDelay(45);
                        serverLevel.addFreshEntity(dropped);
                        player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
                        serverLevel.sendParticles(BornInChaosV1ModParticleTypes.STUNSTARS.get(), player.getX(), player.getY() + 1.5, player.getZ(), 5, 0.25, 0.3, 0.25, 0.1);
                    }
                    if (!player.level().isClientSide()) {
                        player.addEffect(new MobEffectInstance(BornInChaosV1ModMobEffects.LOST_ITEMS, 35, 0, false, false));
                    }
                }
            }
        } else if (target.isBlocking()) {
            ItemStack useItem = target.getUseItem();
            if (useItem.getItem() instanceof ShieldItem || useItem.canPerformAction(ItemAbilities.SHIELD_BLOCK)) {
                Item shieldItemType = useItem.getItem();
                if (world instanceof ServerLevel) {
                    useItem.hurtAndBreak(25, target, LivingEntity.getSlotForHand(target.getUsedItemHand()));
                }
                if (target instanceof Player player) {
                    player.getCooldowns().addCooldown(shieldItemType, 100);
                }
            }
        }
    }
}
