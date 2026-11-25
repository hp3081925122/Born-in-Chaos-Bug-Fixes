package com.hp.borninchaosoptimizer.mixin;

import com.hp.borninchaosoptimizer.config.OptimizerConfig;
import net.mcreator.borninchaosv.entity.LordPumpkinheadEntity;
import net.mcreator.borninchaosv.init.BornInChaosV1ModMobEffects;
import net.mcreator.borninchaosv.init.BornInChaosV1ModParticleTypes;
import net.mcreator.borninchaosv.init.BornInChaosV1ModSounds;
import net.mcreator.borninchaosv.procedures.LordPumpkinheadatackProcedure;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.neoforged.bus.api.Event;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 修复南瓜领主攻击时误用受击目标最大生命值判断自身半血阶段导致二阶段活体炸弹技能失效的缺陷。
 */
@Mixin(value = LordPumpkinheadatackProcedure.class, remap = false)
public abstract class LordPumpkinheadAttackFixMixin {
    @Inject(method = "execute(Lnet/neoforged/bus/api/Event;Lnet/minecraft/world/level/LevelAccessor;DDDLnet/minecraft/world/entity/Entity;Lnet/minecraft/world/entity/Entity;)V", at = @At("HEAD"), cancellable = true)
    private static void bic$fixBossHalfHealthCheck(Event event, LevelAccessor world, double x, double y, double z, Entity entity, Entity sourceentity, CallbackInfo ci) {
        if (!OptimizerConfig.isEnabled(OptimizerConfig.FIX_BOSS_PHASE_HEALTH_CHECK)) {
            return;
        }
        ci.cancel();
        if (!(sourceentity instanceof LordPumpkinheadEntity boss) || !(entity instanceof LivingEntity target)) {
            return;
        }
        if (!(entity instanceof Mob || entity instanceof Player)) {
            return;
        }
        boolean aboveHalfHealth = boss.getHealth() > boss.getMaxHealth() * 0.5f;
        if (aboveHalfHealth) {
            if (!target.hasEffect(BornInChaosV1ModMobEffects.SOUL_STRATIFICATION)) {
                if (!target.level().isClientSide()) {
                    target.addEffect(new MobEffectInstance(BornInChaosV1ModMobEffects.SOUL_STRATIFICATION, 160, 0));
                }
                if (world instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(BornInChaosV1ModParticleTypes.INFERNAL_SURGE.get(), x, y + 1.0, z, 6, 0.2, 0.3, 0.2, 0.1);
                }
                if (!target.isBlocking() && !target.level().isClientSide()) {
                    target.addEffect(new MobEffectInstance(BornInChaosV1ModMobEffects.INFERNAL_FLAME, 160, 0));
                }
            }
        } else {
            if (!target.hasEffect(BornInChaosV1ModMobEffects.LIVING_BOMB)) {
                if (world instanceof Level level) {
                    if (!level.isClientSide()) {
                        level.playSound(null, BlockPos.containing(x, y, z), BornInChaosV1ModSounds.PUMPKINHEAD_BOMB_CURSE.get(), SoundSource.NEUTRAL, 1.0f, 1.0f);
                    } else {
                        level.playLocalSound(x, y, z, BornInChaosV1ModSounds.PUMPKINHEAD_BOMB_CURSE.get(), SoundSource.NEUTRAL, 1.0f, 1.0f, false);
                    }
                }
                if (world instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(BornInChaosV1ModParticleTypes.INFERNAL_SURGE.get(), x, y + 1.0, z, 6, 0.2, 0.3, 0.2, 0.1);
                }
                if (!target.level().isClientSide()) {
                    target.addEffect(new MobEffectInstance(BornInChaosV1ModMobEffects.LIVING_BOMB, 320, 0));
                }
                if (!target.isBlocking() && !target.level().isClientSide()) {
                    target.addEffect(new MobEffectInstance(BornInChaosV1ModMobEffects.INFERNAL_FLAME, 300, 0));
                }
            }
        }
        if (!(entity instanceof Player) && !boss.level().isClientSide()) {
            boss.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 100, 2, false, false));
        }
    }
}
