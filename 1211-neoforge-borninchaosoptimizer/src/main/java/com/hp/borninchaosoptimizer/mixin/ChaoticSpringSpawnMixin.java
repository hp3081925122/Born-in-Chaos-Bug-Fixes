package com.hp.borninchaosoptimizer.mixin;

import com.hp.borninchaosoptimizer.config.OptimizerConfig;
import com.hp.borninchaosoptimizer.util.SeasonalDateCache;
import net.mcreator.borninchaosv.init.BornInChaosV1ModGameRules;
import net.mcreator.borninchaosv.init.BornInChaosV1ModItems;
import net.mcreator.borninchaosv.procedures.ChaoticSpringEventSpawnProcedure;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.LevelAccessor;
import net.neoforged.bus.api.Event;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 修复混沌之春季节事件在区块重载时重复随机覆盖僵尸/骷髅已有装备的问题，并用日期缓存替代高频 Calendar 实例化。
 */
@Mixin(value = ChaoticSpringEventSpawnProcedure.class, remap = false)
public abstract class ChaoticSpringSpawnMixin {
    @Unique
    private static final String BIC_SPRING_CHECKED = "bic_opt_spring_checked";

    @Inject(method = "execute(Lnet/neoforged/bus/api/Event;Lnet/minecraft/world/level/LevelAccessor;Lnet/minecraft/world/entity/Entity;)V", at = @At("HEAD"), cancellable = true)
    private static void bic$fixChaoticSpringSpawn(Event event, LevelAccessor world, Entity entity, CallbackInfo ci) {
        if (!OptimizerConfig.isEnabled(OptimizerConfig.FIX_SEASONAL_RELOAD_OVERWRITE)) {
            return;
        }
        ci.cancel();
        if (world.isClientSide() || !(entity instanceof Zombie || entity instanceof Skeleton)) {
            return;
        }
        if (event instanceof EntityJoinLevelEvent joinEvent && joinEvent.loadedFromDisk()) {
            return;
        }
        GameRules rules = world.getLevelData().getGameRules();
        if (!rules.getBoolean(BornInChaosV1ModGameRules.SEASONAL_EVENTS)) {
            return;
        }
        if (!SeasonalDateCache.isChaoticSpringDate() && !rules.getBoolean(BornInChaosV1ModGameRules.CHAOTIC_SPRING_EVENT)) {
            return;
        }
        CompoundTag data = entity.getPersistentData();
        if (data.getBoolean(BIC_SPRING_CHECKED)) {
            return;
        }
        data.putBoolean(BIC_SPRING_CHECKED, true);

        LivingEntity living = (LivingEntity) entity;
        if (entity instanceof Zombie) {
            if (Math.random() < 0.35) {
                living.setItemSlot(EquipmentSlot.HEAD, new ItemStack(BornInChaosV1ModItems.KILLER_RABBIT_EARS_HELMET.get()));
                living.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(BornInChaosV1ModItems.CARROT_SWORD.get()));
                living.addEffect(new MobEffectInstance(MobEffects.JUMP, Integer.MAX_VALUE, 1, false, false));
            }
        } else if (Math.random() < 0.25) {
            living.setItemSlot(EquipmentSlot.HEAD, new ItemStack(BornInChaosV1ModItems.KILLER_RABBIT_EARS_HELMET.get()));
            living.addEffect(new MobEffectInstance(MobEffects.JUMP, Integer.MAX_VALUE, 1, false, false));
            if (Math.random() < 0.45) {
                living.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(BornInChaosV1ModItems.CARROT_SWORD.get()));
            }
        }
    }
}
