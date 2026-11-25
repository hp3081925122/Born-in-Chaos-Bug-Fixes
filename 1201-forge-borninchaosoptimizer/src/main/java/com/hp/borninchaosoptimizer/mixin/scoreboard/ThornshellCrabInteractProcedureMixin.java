package com.hp.borninchaosoptimizer.mixin.scoreboard;

import com.hp.borninchaosoptimizer.config.BicOptimizerConfig;
import net.mcreator.borninchaosv.entity.ThornshellCrabEntity;
import net.mcreator.borninchaosv.init.BornInChaosV1ModItems;
import net.mcreator.borninchaosv.init.BornInChaosV1ModMobEffects;
import net.mcreator.borninchaosv.init.BornInChaosV1ModParticleTypes;
import net.mcreator.borninchaosv.procedures.ThornshellCrabPriShchielchkiePKMPoSushchnostiProcedure;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
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
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.registries.ForgeRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 替换荆棘蟹喂食交互对原版 Scoreboard("eat") 的写入为实体 PersistentData，
 * 同时修复双端扣物品同步问题。
 */
@Mixin(value = ThornshellCrabPriShchielchkiePKMPoSushchnostiProcedure.class, remap = false)
public abstract class ThornshellCrabInteractProcedureMixin {
    @Unique
    private static final String BIC_KEY_EAT = "bic_opt_eat";
    @Unique
    private static final ResourceLocation BIC_ADV_WASTE = ResourceLocation.tryBuild("born_in_chaos_v1", "waste_recycler");
    @Unique
    private static final ResourceLocation BIC_SND_CRACK = ResourceLocation.tryParse("minecraft:entity.turtle.egg_crack");

    @Inject(
            method = "execute(Lnet/minecraftforge/eventbus/api/Event;Lnet/minecraft/world/level/LevelAccessor;DDDLnet/minecraft/world/entity/Entity;Lnet/minecraft/world/entity/Entity;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void bic$replaceScoreboardWrite(
            Event event, LevelAccessor world, double x, double y, double z, Entity entity, Entity sourceentity, CallbackInfo ci
    ) {
        if (!BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.enableGlobalFixes)
                || !BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.fixScoreboardEntityLeak)) {
            return;
        }
        ci.cancel();
        if (entity == null || sourceentity == null) {
            return;
        }
        if (!(entity instanceof ThornshellCrabEntity crab) || !(sourceentity instanceof LivingEntity feeder)) {
            return;
        }
        if (feeder.hasEffect(BornInChaosV1ModMobEffects.BARBEDATTACK.get())) {
            return;
        }
        ItemStack mainHand = feeder.getMainHandItem();
        if (mainHand.getItem() != BornInChaosV1ModItems.ROTTEN_FISH.get() && mainHand.getItem() != Items.ROTTEN_FLESH) {
            return;
        }
        if (crab.hasEffect(MobEffects.SATURATION) || crab.hasEffect(BornInChaosV1ModMobEffects.BARBEDATTACK.get())) {
            return;
        }
        feeder.swing(InteractionHand.MAIN_HAND, true);
        if (world.isClientSide()) {
            return;
        }
        if (sourceentity instanceof ServerPlayer sp) {
            Advancement adv = sp.server.getAdvancements().getAdvancement(BIC_ADV_WASTE);
            if (adv != null) {
                AdvancementProgress ap = sp.getAdvancements().getOrStartProgress(adv);
                if (!ap.isDone()) {
                    for (String criterion : ap.getRemainingCriteria()) {
                        sp.getAdvancements().award(adv, criterion);
                    }
                }
            }
        }
        crab.getPersistentData().putBoolean(BIC_KEY_EAT, true);
        mainHand.shrink(1);
        if (sourceentity instanceof Player player) {
            player.getCooldowns().addCooldown(BornInChaosV1ModItems.ROTTEN_FISH.get(), 15);
            player.getCooldowns().addCooldown(Items.ROTTEN_FLESH, 15);
        }
        if (world instanceof Level level) {
            SoundEvent sound = ForgeRegistries.SOUND_EVENTS.getValue(BIC_SND_CRACK);
            if (sound != null) {
                level.playSound(null, BlockPos.containing(x, y, z), sound, SoundSource.NEUTRAL, 0.1f, 1.0f);
            }
        }
        if (world instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(
                    BornInChaosV1ModParticleTypes.FLESHSPLASH.get(),
                    crab.getX(), crab.getY() + 0.5, crab.getZ(),
                    3, 0.2, 0.2, 0.2, 0.1
            );
        }
        crab.addEffect(new MobEffectInstance(MobEffects.SATURATION, 15, 0, false, false));
        crab.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 15, 5, false, false));
        crab.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 15, 5, false, false));
        crab.addEffect(new MobEffectInstance(BornInChaosV1ModMobEffects.BARBEDATTACK.get(), 15, 0, false, false));
        feeder.addEffect(new MobEffectInstance(BornInChaosV1ModMobEffects.BARBEDATTACK.get(), 15, 0, false, false));
    }
}
