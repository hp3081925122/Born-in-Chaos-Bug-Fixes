package com.hp.borninchaosoptimizer.mixin.combat;

import com.hp.borninchaosoptimizer.config.BicOptimizerConfig;
import net.mcreator.borninchaosv.entity.LordPumpkinheadEntity;
import net.mcreator.borninchaosv.entity.LordTheHeadlessEntity;
import net.mcreator.borninchaosv.init.BornInChaosV1ModMobEffects;
import net.mcreator.borninchaosv.init.BornInChaosV1ModParticleTypes;
import net.mcreator.borninchaosv.procedures.LordPumpkinheadatackProcedure;
import net.mcreator.borninchaosv.procedures.LordTheHeadlessAtackProcedure;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LevelAccessor;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.registries.ForgeRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 修复南瓜领主与无头领主攻击过程的两处同源严重逻辑缺陷：
 * 1. 原代码误将首领当前生命值与受击目标（如 20 血玩家）的最大生命值比较，导致二阶段挂活体炸弹技能永久沦为死代码；
 *    现按首领自身最大生命值百分比判定（南瓜领主 283/666 血即原式半血减 50，无头领主 50% 血）；
 * 2. 原无头领主在攻击非玩家生物时，误将力量 III（DAMAGE_BOOST）加到了挨打敌人身上而非首领自身。
 */
@Mixin(value = {
        LordPumpkinheadatackProcedure.class,
        LordTheHeadlessAtackProcedure.class
}, remap = false)
public abstract class LordBossAttackProceduresMixin {
    @Unique
    private static final ResourceLocation BIC_BOMB_CURSE_SOUND =
            ResourceLocation.tryParse("born_in_chaos_v1:pumpkinhead_bomb_curse");

    @Inject(
            method = "execute(Lnet/minecraftforge/eventbus/api/Event;Lnet/minecraft/world/level/LevelAccessor;DDDLnet/minecraft/world/entity/Entity;Lnet/minecraft/world/entity/Entity;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void bic$fixLordBossAttack(
            Event event,
            LevelAccessor world,
            double x,
            double y,
            double z,
            Entity entity,
            Entity sourceentity,
            CallbackInfo ci
    ) {
        if (!BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.enableGlobalFixes)
                || !BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.fixBossAttackHealthAndBuff)) {
            return;
        }
        ci.cancel();
        if (entity == null || sourceentity == null || world.isClientSide()) {
            return;
        }
        if (!(entity instanceof LivingEntity target) || !(entity instanceof Mob || entity instanceof Monster || entity instanceof Player)) {
            return;
        }

        if (sourceentity instanceof LordPumpkinheadEntity boss) {
            boolean phaseOne = boss.getHealth() > boss.getMaxHealth() * (283.0f / 666.0f);
            if (phaseOne) {
                if (!target.hasEffect(BornInChaosV1ModMobEffects.SOUL_STRATIFICATION.get())) {
                    target.addEffect(new MobEffectInstance(BornInChaosV1ModMobEffects.SOUL_STRATIFICATION.get(), 160, 0));
                    if (world instanceof ServerLevel serverLevel) {
                        serverLevel.sendParticles(BornInChaosV1ModParticleTypes.INFERNAL_SURGE.get(), x, y + 1.0, z, 6, 0.2, 0.3, 0.2, 0.1);
                    }
                    if (!target.isBlocking()) {
                        target.addEffect(new MobEffectInstance(BornInChaosV1ModMobEffects.INFERNAL_FLAME.get(), 160, 0));
                    }
                }
            } else {
                if (!target.hasEffect(BornInChaosV1ModMobEffects.LIVING_BOMB.get())) {
                    if (world instanceof ServerLevel serverLevel) {
                        SoundEvent sound = BIC_BOMB_CURSE_SOUND != null ? ForgeRegistries.SOUND_EVENTS.getValue(BIC_BOMB_CURSE_SOUND) : null;
                        if (sound != null) {
                            serverLevel.playSound(null, BlockPos.containing(x, y, z), sound, SoundSource.NEUTRAL, 1.0f, 1.0f);
                        }
                        serverLevel.sendParticles(BornInChaosV1ModParticleTypes.INFERNAL_SURGE.get(), x, y + 1.0, z, 6, 0.2, 0.3, 0.2, 0.1);
                    }
                    target.addEffect(new MobEffectInstance(BornInChaosV1ModMobEffects.LIVING_BOMB.get(), 320, 0));
                    if (!target.isBlocking()) {
                        target.addEffect(new MobEffectInstance(BornInChaosV1ModMobEffects.INFERNAL_FLAME.get(), 300, 0));
                    }
                }
            }
            if (!(entity instanceof Player)) {
                boss.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 100, 2, false, false));
            }
        } else if (sourceentity instanceof LordTheHeadlessEntity headless) {
            boolean phaseOne = headless.getHealth() > headless.getMaxHealth() * 0.5f;
            if (phaseOne) {
                if (!target.hasEffect(BornInChaosV1ModMobEffects.SOUL_STRATIFICATION.get())) {
                    target.addEffect(new MobEffectInstance(BornInChaosV1ModMobEffects.SOUL_STRATIFICATION.get(), 180, 0));
                    if (!target.isBlocking()) {
                        target.addEffect(new MobEffectInstance(BornInChaosV1ModMobEffects.INFERNAL_FLAME.get(), 140, 0));
                    }
                }
            } else {
                if (!target.hasEffect(BornInChaosV1ModMobEffects.LIVING_BOMB.get())) {
                    target.addEffect(new MobEffectInstance(BornInChaosV1ModMobEffects.LIVING_BOMB.get(), 260, 0));
                    if (world instanceof ServerLevel serverLevel) {
                        SoundEvent sound = BIC_BOMB_CURSE_SOUND != null ? ForgeRegistries.SOUND_EVENTS.getValue(BIC_BOMB_CURSE_SOUND) : null;
                        if (sound != null) {
                            serverLevel.playSound(null, BlockPos.containing(x, y, z), sound, SoundSource.NEUTRAL, 1.0f, 1.0f);
                        }
                    }
                    if (!target.isBlocking()) {
                        target.addEffect(new MobEffectInstance(BornInChaosV1ModMobEffects.INFERNAL_FLAME.get(), 240, 0));
                    }
                }
            }
            if (!(entity instanceof Player)) {
                headless.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 100, 2, false, false));
            }
        }
    }
}
