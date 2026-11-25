package com.hp.borninchaosoptimizer.mixin;

import com.hp.borninchaosoptimizer.config.OptimizerConfig;
import net.mcreator.borninchaosv.entity.LifestealerTrueFormEntity;
import net.mcreator.borninchaosv.init.BornInChaosV1ModMobEffects;
import net.mcreator.borninchaosv.init.BornInChaosV1ModParticleTypes;
import net.mcreator.borninchaosv.procedures.LifestealerTFAtackProcedure;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.level.LevelAccessor;
import net.neoforged.bus.api.Event;
import net.neoforged.neoforge.common.ItemAbilities;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 修复噬命者真身破盾缺少持有者上下文的问题，并防止噬命者在同刻死亡后因回血被设为正血量而进入假死状态。
 */
@Mixin(value = LifestealerTFAtackProcedure.class, remap = false)
public abstract class LifestealerTFAttackFixMixin {
    @Inject(method = "execute(Lnet/neoforged/bus/api/Event;Lnet/minecraft/world/level/LevelAccessor;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/entity/Entity;)V", at = @At("HEAD"), cancellable = true)
    private static void bic$fixLifestealerAttack(Event event, LevelAccessor world, Entity entity, Entity sourceentity, CallbackInfo ci) {
        if (!OptimizerConfig.isEnabled(OptimizerConfig.FIX_EQUIPMENT_DURABILITY_CONTEXT)) {
            return;
        }
        ci.cancel();
        if (!(sourceentity instanceof LifestealerTrueFormEntity lifestealer) || !(entity instanceof LivingEntity target)) {
            return;
        }
        if (!target.isBlocking()) {
            if (!target.level().isClientSide()) {
                if (target.hasEffect(BornInChaosV1ModMobEffects.LIFESTEAL)) {
                    target.addEffect(new MobEffectInstance(BornInChaosV1ModMobEffects.STRANGLEHOLD, 360, 0));
                }
                target.addEffect(new MobEffectInstance(BornInChaosV1ModMobEffects.LIFESTEAL, 260, 0));
            }
            if (lifestealer.isAlive()) {
                lifestealer.setHealth(Math.min(lifestealer.getMaxHealth(), lifestealer.getHealth() + 5.0f));
            }
            if (world instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(BornInChaosV1ModParticleTypes.RITUAL.get(), lifestealer.getX(), lifestealer.getY() + 2.0, lifestealer.getZ(), 10, 0.5, 1.0, 0.5, 0.0);
            }
        } else {
            ItemStack useItem = target.getUseItem();
            if (useItem.getItem() instanceof ShieldItem || useItem.canPerformAction(ItemAbilities.SHIELD_BLOCK)) {
                if (world instanceof ServerLevel) {
                    useItem.hurtAndBreak(20, target, LivingEntity.getSlotForHand(target.getUsedItemHand()));
                }
                if (!lifestealer.level().isClientSide()) {
                    lifestealer.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 15, 1, false, false));
                }
            }
        }
    }
}
