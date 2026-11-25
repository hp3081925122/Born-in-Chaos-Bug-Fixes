package com.hp.borninchaosoptimizer.mixin;

import com.hp.borninchaosoptimizer.config.OptimizerConfig;
import net.mcreator.borninchaosv.entity.ThornshellCrabEntity;
import net.mcreator.borninchaosv.init.BornInChaosV1ModItems;
import net.mcreator.borninchaosv.init.BornInChaosV1ModMobEffects;
import net.mcreator.borninchaosv.init.BornInChaosV1ModParticleTypes;
import net.mcreator.borninchaosv.procedures.ThornshellCrabPriShchielchkiePKMPoSushchnostiProcedure;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.neoforged.bus.api.Event;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 修复喂食刺壳蟹时向全局计分板写入永久实体 UUID 分数以及创造模式误扣物品的问题。
 */
@Mixin(value = ThornshellCrabPriShchielchkiePKMPoSushchnostiProcedure.class, remap = false)
public abstract class ThornshellCrabWriteScoreMixin {
    @Unique
    private static final ResourceLocation BIC_WASTE_RECYCLER_ADV = ResourceLocation.fromNamespaceAndPath("born_in_chaos_v1", "waste_recycler");

    @Inject(method = "execute(Lnet/neoforged/bus/api/Event;Lnet/minecraft/world/level/LevelAccessor;DDDLnet/minecraft/world/entity/Entity;Lnet/minecraft/world/entity/Entity;)V", at = @At("HEAD"), cancellable = true)
    private static void bic$replaceScoreboardWrite(Event event, LevelAccessor world, double x, double y, double z, Entity entity, Entity sourceentity, CallbackInfo ci) {
        if (!OptimizerConfig.isEnabled(OptimizerConfig.FIX_SCOREBOARD_LEAK)) {
            return;
        }
        ci.cancel();
        if (!(entity instanceof ThornshellCrabEntity crab) || !(sourceentity instanceof LivingEntity feeder)) {
            return;
        }
        if (feeder.hasEffect(BornInChaosV1ModMobEffects.BARBEDATTACK)) {
            return;
        }
        ItemStack mainHand = feeder.getMainHandItem();
        if (mainHand.getItem() != BornInChaosV1ModItems.ROTTEN_FISH.get() && mainHand.getItem() != Items.ROTTEN_FLESH) {
            return;
        }
        if (crab.hasEffect(MobEffects.SATURATION) || crab.hasEffect(BornInChaosV1ModMobEffects.BARBEDATTACK)) {
            return;
        }
        if (feeder instanceof ServerPlayer serverPlayer) {
            AdvancementHolder adv = serverPlayer.server.getAdvancements().get(BIC_WASTE_RECYCLER_ADV);
            if (adv != null) {
                AdvancementProgress progress = serverPlayer.getAdvancements().getOrStartProgress(adv);
                if (!progress.isDone()) {
                    for (String criteria : progress.getRemainingCriteria()) {
                        serverPlayer.getAdvancements().award(adv, criteria);
                    }
                }
            }
        }
        crab.getPersistentData().putBoolean("bic_eat", true);
        crab.getPersistentData().putBoolean("bic_player_interacted", true);
        crab.setPersistenceRequired();
        feeder.swing(InteractionHand.MAIN_HAND, true);
        if (!(feeder instanceof Player player && player.getAbilities().instabuild) && !world.isClientSide()) {
            mainHand.shrink(1);
        }
        if (feeder instanceof Player player) {
            player.getCooldowns().addCooldown(BornInChaosV1ModItems.ROTTEN_FISH.get(), 15);
            player.getCooldowns().addCooldown(Items.ROTTEN_FLESH, 15);
        }
        if (world instanceof Level level) {
            if (!level.isClientSide()) {
                level.playSound(null, BlockPos.containing(x, y, z), SoundEvents.TURTLE_EGG_CRACK, SoundSource.NEUTRAL, 0.1f, 1.0f);
            } else {
                level.playLocalSound(x, y, z, SoundEvents.TURTLE_EGG_CRACK, SoundSource.NEUTRAL, 0.1f, 1.0f, false);
            }
        }
        if (world instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(BornInChaosV1ModParticleTypes.FLESHSPLASH.get(), crab.getX(), crab.getY() + 0.5, crab.getZ(), 3, 0.2, 0.2, 0.2, 0.1);
        }
        if (!crab.level().isClientSide()) {
            crab.addEffect(new MobEffectInstance(MobEffects.SATURATION, 15, 0, false, false));
            crab.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 15, 5, false, false));
            crab.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 15, 5, false, false));
            crab.addEffect(new MobEffectInstance(BornInChaosV1ModMobEffects.BARBEDATTACK, 15, 0, false, false));
        }
        if (!feeder.level().isClientSide()) {
            feeder.addEffect(new MobEffectInstance(BornInChaosV1ModMobEffects.BARBEDATTACK, 15, 0, false, false));
        }
    }
}
