package com.hp.borninchaosoptimizer.mixin.combat;

import com.hp.borninchaosoptimizer.config.BicOptimizerConfig;
import net.mcreator.borninchaosv.entity.KrampusHenchmanEntity;
import net.mcreator.borninchaosv.init.BornInChaosV1ModMobEffects;
import net.mcreator.borninchaosv.init.BornInChaosV1ModParticleTypes;
import net.mcreator.borninchaosv.procedures.KrampusHenchmanAttackProcedure;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
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
 * 修复 KrampusHenchmanAttackProcedure 双端独立随机删客户端主手物品与无意义遍历深拷贝 41 格物品栏的问题。
 */
@Mixin(value = KrampusHenchmanAttackProcedure.class, remap = false)
public abstract class KrampusHenchmanAttackProcedureMixin {
    @Unique
    private static final ResourceLocation BIC_SND_MINION_BLOW = ResourceLocation.tryBuild("born_in_chaos_v1", "minion_blow");

    @Inject(
            method = "execute(Lnet/minecraftforge/eventbus/api/Event;Lnet/minecraft/world/level/LevelAccessor;DDDLnet/minecraft/world/entity/Entity;Lnet/minecraft/world/entity/Entity;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void bic$fixHenchmanAttack(
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
        if (!(sourceentity instanceof KrampusHenchmanEntity) || !(entity instanceof Player player)) {
            return;
        }
        if (player.isBlocking() || !(Math.random() < 0.35)) {
            return;
        }
        ItemStack mainHand = player.getMainHandItem();
        if (mainHand.isEmpty() || mainHand.getMaxStackSize() != 1) {
            return;
        }
        if (world instanceof Level level) {
            SoundEvent sound = ForgeRegistries.SOUND_EVENTS.getValue(BIC_SND_MINION_BLOW);
            if (sound != null) {
                level.playSound(null, BlockPos.containing(x, y, z), sound, SoundSource.NEUTRAL, 1.0f, 1.0f);
            }
        }
        if (world instanceof ServerLevel serverLevel) {
            ItemEntity dropped = new ItemEntity(
                    serverLevel,
                    player.getX(), player.getY() + 1.0, player.getZ(),
                    mainHand.copy()
            );
            dropped.setPickUpDelay(45);
            serverLevel.addFreshEntity(dropped);
            player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
            serverLevel.sendParticles(
                    BornInChaosV1ModParticleTypes.STUNSTARS.get(),
                    player.getX(), player.getY() + 1.3, player.getZ(),
                    3, 0.25, 0.3, 0.25, 0.1
            );
        }
        player.addEffect(new MobEffectInstance(BornInChaosV1ModMobEffects.LOST_ITEMS.get(), 35, 0, false, false));
    }
}
