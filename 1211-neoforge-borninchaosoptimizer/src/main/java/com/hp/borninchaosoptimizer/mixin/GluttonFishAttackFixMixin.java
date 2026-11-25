package com.hp.borninchaosoptimizer.mixin;

import com.hp.borninchaosoptimizer.config.OptimizerConfig;
import net.mcreator.borninchaosv.entity.GluttonFishEntity;
import net.mcreator.borninchaosv.init.BornInChaosV1ModMobEffects;
import net.mcreator.borninchaosv.init.BornInChaosV1ModParticleTypes;
import net.mcreator.borninchaosv.init.BornInChaosV1ModSounds;
import net.mcreator.borninchaosv.procedures.GluttonfishatackProcedure;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
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
 * 修复暴食鱼攻击玩家盾牌时缺少持有者与槽位上下文的问题，并复用音效常量消除运行时字符串解析。
 */
@Mixin(value = GluttonfishatackProcedure.class, remap = false)
public abstract class GluttonFishAttackFixMixin {
    @Inject(method = "execute(Lnet/neoforged/bus/api/Event;Lnet/minecraft/world/level/LevelAccessor;DDDLnet/minecraft/world/entity/Entity;Lnet/minecraft/world/entity/Entity;)V", at = @At("HEAD"), cancellable = true)
    private static void bic$fixGluttonFishAttack(Event event, LevelAccessor world, double x, double y, double z, Entity entity, Entity sourceentity, CallbackInfo ci) {
        if (!OptimizerConfig.isEnabled(OptimizerConfig.FIX_EQUIPMENT_DURABILITY_CONTEXT)) {
            return;
        }
        ci.cancel();
        if (!(sourceentity instanceof GluttonFishEntity fish) || !(entity instanceof LivingEntity target)) {
            return;
        }
        if (!(entity instanceof Mob || entity instanceof Player)) {
            return;
        }
        if (!target.isBlocking()) {
            if (!target.level().isClientSide()) {
                target.addEffect(new MobEffectInstance(BornInChaosV1ModMobEffects.BONE_FRACTURE, 100, 0));
            }
            if (world instanceof Level level) {
                if (!level.isClientSide()) {
                    level.playSound(null, BlockPos.containing(x, y, z), BornInChaosV1ModSounds.GLUTTON_FISH_ATTACK.get(), SoundSource.NEUTRAL, 0.8f, 1.0f);
                } else {
                    level.playLocalSound(x, y, z, BornInChaosV1ModSounds.GLUTTON_FISH_ATTACK.get(), SoundSource.NEUTRAL, 0.8f, 1.0f, false);
                }
            }
        } else if (target instanceof Player player) {
            if (!fish.level().isClientSide()) {
                fish.addEffect(new MobEffectInstance(BornInChaosV1ModMobEffects.STUN, 60, 0, false, false));
            }
            if (world instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(BornInChaosV1ModParticleTypes.STUNSTARS.get(), x, y, z, 9, 1.0, 1.0, 1.0, 0.1);
                ItemStack useItem = player.getUseItem();
                if (useItem.getItem() instanceof ShieldItem || useItem.canPerformAction(ItemAbilities.SHIELD_BLOCK)) {
                    useItem.hurtAndBreak(20, player, LivingEntity.getSlotForHand(player.getUsedItemHand()));
                } else if (player.getMainHandItem().is(Items.SHIELD)) {
                    player.getMainHandItem().hurtAndBreak(20, player, EquipmentSlot.MAINHAND);
                } else if (player.getOffhandItem().is(Items.SHIELD)) {
                    player.getOffhandItem().hurtAndBreak(20, player, EquipmentSlot.OFFHAND);
                }
            }
        }
    }
}
