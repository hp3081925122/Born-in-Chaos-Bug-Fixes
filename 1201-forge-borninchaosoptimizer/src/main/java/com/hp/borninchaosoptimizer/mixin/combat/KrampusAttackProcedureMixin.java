package com.hp.borninchaosoptimizer.mixin.combat;

import com.hp.borninchaosoptimizer.config.BicOptimizerConfig;
import net.mcreator.borninchaosv.entity.KrampusEntity;
import net.mcreator.borninchaosv.init.BornInChaosV1ModMobEffects;
import net.mcreator.borninchaosv.init.BornInChaosV1ModParticleTypes;
import net.mcreator.borninchaosv.procedures.KrampusAttackProcedure;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.registries.ForgeRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 修复 KrampusAttackProcedure 的三大缺陷：
 * 1. 客户端与服务端独立执行 Math.random() < 0.35 并在客户端 shrink 主手物品造成幽灵空槽；
 * 2. 玩家空手时生成 ItemStack.EMPTY 的空掉落物实体；
 * 3. 无意义遍历玩家全部 41 格物品栏并逐格执行 ItemStack.copy() 深拷贝。
 */
@Mixin(value = KrampusAttackProcedure.class, remap = false)
public abstract class KrampusAttackProcedureMixin {
    @Unique
    private static final ResourceLocation BIC_SND_KRAMPUS_BLOW = ResourceLocation.tryBuild("born_in_chaos_v1", "krampus_blow");

    @Inject(
            method = "execute(Lnet/minecraftforge/eventbus/api/Event;Lnet/minecraft/world/level/LevelAccessor;DDDLnet/minecraft/world/entity/Entity;Lnet/minecraft/world/entity/Entity;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void bic$fixKrampusAttack(
            Event event, LevelAccessor world, double x, double y, double z, Entity entity, Entity sourceentity, CallbackInfo ci
    ) {
        if (!BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.enableGlobalFixes)
                || !BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.fixGlobalCombatEvents)) {
            return;
        }
        ci.cancel();
        if (entity == null || sourceentity == null || world.isClientSide()) {
            return;
        }
        if (!(sourceentity instanceof KrampusEntity krampus) || !(entity instanceof LivingEntity target)) {
            return;
        }
        if (!(entity instanceof Mob || entity instanceof Monster || entity instanceof Player)) {
            return;
        }
        if (!target.isBlocking()) {
            if (Math.random() < 0.35) {
                if (world instanceof Level level) {
                    SoundEvent sound = ForgeRegistries.SOUND_EVENTS.getValue(BIC_SND_KRAMPUS_BLOW);
                    if (sound != null) {
                        level.playSound(null, BlockPos.containing(x, y, z), sound, SoundSource.NEUTRAL, 1.0f, 1.0f);
                    }
                }
                if (target.hasEffect(BornInChaosV1ModMobEffects.BONE_CHILLING.get())) {
                    krampus.setHealth(krampus.getHealth() + 8.0f);
                }
                if (entity instanceof Player player) {
                    ItemStack mainHand = player.getMainHandItem();
                    if (!mainHand.isEmpty() && world instanceof ServerLevel serverLevel) {
                        ItemEntity dropped = new ItemEntity(
                                serverLevel,
                                player.getX(), player.getY() + 1.0, player.getZ(),
                                mainHand.copy()
                        );
                        dropped.setPickUpDelay(45);
                        serverLevel.addFreshEntity(dropped);
                        player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
                    }
                    if (world instanceof ServerLevel serverLevel) {
                        serverLevel.sendParticles(
                                BornInChaosV1ModParticleTypes.STUNSTARS.get(),
                                player.getX(), player.getY() + 1.5, player.getZ(),
                                5, 0.25, 0.3, 0.25, 0.1
                        );
                    }
                    player.addEffect(new MobEffectInstance(BornInChaosV1ModMobEffects.LOST_ITEMS.get(), 35, 0, false, false));
                }
            }
        } else {
            ItemStack useItem = target.getUseItem();
            if (useItem.getItem() instanceof ShieldItem) {
                InteractionHand usedHand = target.getUsedItemHand();
                if (entity instanceof Player player) {
                    player.getCooldowns().addCooldown(useItem.getItem(), 100);
                }
                useItem.hurtAndBreak(25, target, brokenTarget -> brokenTarget.broadcastBreakEvent(usedHand));
            }
        }
    }
}
