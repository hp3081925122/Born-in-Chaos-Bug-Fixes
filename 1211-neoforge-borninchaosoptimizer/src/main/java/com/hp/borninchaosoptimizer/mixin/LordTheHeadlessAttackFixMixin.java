package com.hp.borninchaosoptimizer.mixin;

import com.hp.borninchaosoptimizer.config.OptimizerConfig;
import net.mcreator.borninchaosv.entity.LordTheHeadlessEntity;
import net.mcreator.borninchaosv.init.BornInChaosV1ModMobEffects;
import net.mcreator.borninchaosv.init.BornInChaosV1ModSounds;
import net.mcreator.borninchaosv.procedures.LordTheHeadlessAtackProcedure;
import net.minecraft.core.BlockPos;
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
 * 修复无头领主攻击时误用受击目标最大生命值判定半血阶段，以及在攻击非玩家生物时把力量 III 误加给挨打敌人的缺陷。
 */
@Mixin(value = LordTheHeadlessAtackProcedure.class, remap = false)
public abstract class LordTheHeadlessAttackFixMixin {
    @Inject(method = "execute(Lnet/neoforged/bus/api/Event;Lnet/minecraft/world/level/LevelAccessor;DDDLnet/minecraft/world/entity/Entity;Lnet/minecraft/world/entity/Entity;)V", at = @At("HEAD"), cancellable = true)
    private static void bic$fixHeadlessAttackLogic(Event event, LevelAccessor world, double x, double y, double z, Entity entity, Entity sourceentity, CallbackInfo ci) {
        if (!OptimizerConfig.isEnabled(OptimizerConfig.FIX_BOSS_PHASE_HEALTH_CHECK)) {
            return;
        }
        ci.cancel();
        if (!(sourceentity instanceof LordTheHeadlessEntity boss) || !(entity instanceof LivingEntity target)) {
            return;
        }
        if (!(entity instanceof Mob || entity instanceof Player)) {
            return;
        }
        boolean aboveHalfHealth = boss.getHealth() > boss.getMaxHealth() * 0.5f;
        if (aboveHalfHealth) {
            if (!target.hasEffect(BornInChaosV1ModMobEffects.SOUL_STRATIFICATION)) {
                if (!target.level().isClientSide()) {
                    target.addEffect(new MobEffectInstance(BornInChaosV1ModMobEffects.SOUL_STRATIFICATION, 180, 0));
                }
                if (!target.isBlocking() && !target.level().isClientSide()) {
                    target.addEffect(new MobEffectInstance(BornInChaosV1ModMobEffects.INFERNAL_FLAME, 140, 0));
                }
            }
        } else {
            if (!target.hasEffect(BornInChaosV1ModMobEffects.LIVING_BOMB)) {
                if (!target.level().isClientSide()) {
                    target.addEffect(new MobEffectInstance(BornInChaosV1ModMobEffects.LIVING_BOMB, 260, 0));
                }
                if (world instanceof Level level) {
                    if (!level.isClientSide()) {
                        level.playSound(null, BlockPos.containing(x, y, z), BornInChaosV1ModSounds.PUMPKINHEAD_BOMB_CURSE.get(), SoundSource.NEUTRAL, 1.0f, 1.0f);
                    } else {
                        level.playLocalSound(x, y, z, BornInChaosV1ModSounds.PUMPKINHEAD_BOMB_CURSE.get(), SoundSource.NEUTRAL, 1.0f, 1.0f, false);
                    }
                }
                if (!target.isBlocking() && !target.level().isClientSide()) {
                    target.addEffect(new MobEffectInstance(BornInChaosV1ModMobEffects.INFERNAL_FLAME, 240, 0));
                }
            }
        }
        // 原代码误写为给受击者 entity 添加 DAMAGE_BOOST，修正为给施法首领自身添加
        if (!(entity instanceof Player) && !boss.level().isClientSide()) {
            boss.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 100, 2, false, false));
        }
    }
}
