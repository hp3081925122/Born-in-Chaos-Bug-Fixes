package com.hp.borninchaosoptimizer.mixin;

import com.hp.borninchaosoptimizer.config.OptimizerConfig;
import net.mcreator.borninchaosv.entity.FelsteedEntity;
import net.mcreator.borninchaosv.entity.InfernalSpiritEntity;
import net.mcreator.borninchaosv.entity.LordPumpkinheadEntity;
import net.mcreator.borninchaosv.entity.LordPumpkinheadHeadEntity;
import net.mcreator.borninchaosv.entity.LordPumpkinheadWithoutaHorseEntity;
import net.mcreator.borninchaosv.entity.LordTheHeadlessEntity;
import net.mcreator.borninchaosv.entity.LordsFelsteedEntity;
import net.mcreator.borninchaosv.entity.PumpkinheadEntity;
import net.mcreator.borninchaosv.entity.SirPumpkinheadEntity;
import net.mcreator.borninchaosv.entity.SirPumpkinheadWithoutHorseEntity;
import net.mcreator.borninchaosv.entity.SirTheHeadlessEntity;
import net.mcreator.borninchaosv.init.BornInChaosV1ModMobEffects;
import net.mcreator.borninchaosv.init.BornInChaosV1ModParticleTypes;
import net.mcreator.borninchaosv.procedures.InfernalFlameKazhdyiTikVoVriemiaEffiektaProcedure;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.LevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 修复地狱火（Infernal Flame）每刻过程：
 * 1. 仅在服务端权威执行，消除客户端独立递减 infernal 计时器、客户端无效 hurt 与本地重叠音效；
 * 2. 对免疫地狱火的南瓜系实体增加 hasEffect 前置判断后再调用 removeEffect，防止无条件广播 MobEffectEvent.Remove。
 */
@Mixin(value = InfernalFlameKazhdyiTikVoVriemiaEffiektaProcedure.class, remap = false)
public abstract class InfernalFlameOptimizeMixin {
    @Inject(method = "execute", at = @At("HEAD"), cancellable = true)
    private static void bic$optimizeInfernalFlame(LevelAccessor world, double x, double y, double z, Entity entity, CallbackInfo ci) {
        if (!OptimizerConfig.isEnabled(OptimizerConfig.OPTIMIZE_EFFECT_TICK_SCANS)) {
            return;
        }
        ci.cancel();
        if (entity == null || world.isClientSide() || !(world instanceof ServerLevel serverLevel)) {
            return;
        }

        serverLevel.sendParticles(BornInChaosV1ModParticleTypes.ANIM_FIRE.get(), x, y + 1.0, z, 1, 0.3, 0.3, 0.3, 0.1);

        if (bic$isInfernalImmuneEntity(entity) && entity instanceof LivingEntity living) {
            if (living.hasEffect(BornInChaosV1ModMobEffects.INFERNAL_FLAME)) {
                living.removeEffect(BornInChaosV1ModMobEffects.INFERNAL_FLAME);
            }
            return;
        }

        double timer = entity.getPersistentData().getDouble("infernal");
        timer = (timer <= 0.0) ? 40.0 : (timer - 1.0);
        entity.getPersistentData().putDouble("infernal", timer);

        if (timer != 0.0) {
            return;
        }
        if (entity instanceof LivingEntity living && living.hasEffect(MobEffects.FIRE_RESISTANCE)) {
            return;
        }

        float health = entity instanceof LivingEntity living ? living.getHealth() : -1.0f;
        float damage;
        if (health >= 30.0f) {
            damage = 5.0f;
        } else if (health > 6.0f) {
            damage = 3.0f;
        } else if (health > 3.0f) {
            damage = 2.0f;
        } else {
            damage = 1.0f;
        }

        entity.hurt(new DamageSource(serverLevel.holderOrThrow(DamageTypes.IN_FIRE)), damage);
        serverLevel.sendParticles(BornInChaosV1ModParticleTypes.INFERNAL_SURGE.get(), x, y + 1.0, z, 3, 0.3, 0.3, 0.3, 0.1);
        serverLevel.playSound(null, BlockPos.containing(x, y, z), SoundEvents.PLAYER_HURT_ON_FIRE, SoundSource.NEUTRAL, 0.9f, 0.8f);
    }

    @Unique
    private static boolean bic$isInfernalImmuneEntity(Entity entity) {
        return entity instanceof LordPumpkinheadEntity
                || entity instanceof SirTheHeadlessEntity
                || entity instanceof PumpkinheadEntity
                || entity instanceof FelsteedEntity
                || entity instanceof SirPumpkinheadEntity
                || entity instanceof SirPumpkinheadWithoutHorseEntity
                || entity instanceof LordPumpkinheadHeadEntity
                || entity instanceof LordPumpkinheadWithoutaHorseEntity
                || entity instanceof LordsFelsteedEntity
                || entity instanceof InfernalSpiritEntity
                || entity instanceof LordTheHeadlessEntity;
    }
}
