package com.hp.borninchaosoptimizer.mixin.potion;

import com.hp.borninchaosoptimizer.config.BicOptimizerConfig;
import net.mcreator.borninchaosv.init.BornInChaosV1ModMobEffects;
import net.mcreator.borninchaosv.init.BornInChaosV1ModParticleTypes;
import net.mcreator.borninchaosv.procedures.LivingBombKazhdyiTikVoVriemiaEffiektaProcedure;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

/**
 * 优化活体炸弹（Living Bomb）：仅服务端权威执行，直接扫描 LivingEntity.class 消除全实体流排序，
 * 并在清除正面状态前检查 hasEffect 避免无效事件广播。
 */
@Mixin(value = LivingBombKazhdyiTikVoVriemiaEffiektaProcedure.class, remap = false)
public abstract class LivingBombProcedureMixin {
    private static final ResourceLocation PORTAL_SPAWN_SOUND_ID = ResourceLocation.tryParse("minecraft:block.end_portal.spawn");

    @Inject(method = "execute", at = @At("HEAD"), cancellable = true)
    private static void bicOptimizer$optimizeLivingBomb(LevelAccessor world, double x, double y, double z, Entity entity, CallbackInfo ci) {
        if (!BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.enableGlobalFixes)
                || !BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.optimizePotionAuraAndStateTicks)) {
            return;
        }
        ci.cancel();
        if (!(entity instanceof LivingEntity living) || !(world instanceof ServerLevel serverLevel)) {
            return;
        }

        MobEffectInstance bombEffect = living.getEffect(BornInChaosV1ModMobEffects.LIVING_BOMB.get());
        if (bombEffect == null || bombEffect.getDuration() != 10) {
            return;
        }

        float health = living.getHealth();
        if (health >= 100.0f) {
            serverLevel.explode(null, x, y + 1.0, z, 6.0f, Level.ExplosionInteraction.MOB);
        } else if (health >= 50.0f) {
            serverLevel.explode(null, x, y + 1.0, z, 5.0f, Level.ExplosionInteraction.MOB);
        } else if (health >= 30.0f) {
            serverLevel.explode(null, x, y + 1.0, z, 4.0f, Level.ExplosionInteraction.MOB);
        } else if (health > 0.0f) {
            serverLevel.explode(null, x, y + 1.0, z, 3.0f, Level.ExplosionInteraction.MOB);
        }

        if (living.ignoreExplosion()) {
            DamageSource genericDamage = new DamageSource(
                    serverLevel.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(DamageTypes.GENERIC)
            );
            living.hurt(genericDamage, 30.0f);
        }

        SoundEvent sound = PORTAL_SPAWN_SOUND_ID != null ? ForgeRegistries.SOUND_EVENTS.getValue(PORTAL_SPAWN_SOUND_ID) : null;
        if (sound != null) {
            serverLevel.playSound(null, BlockPos.containing(x, y, z), sound, SoundSource.NEUTRAL, 0.3f, 0.6f);
        }
        serverLevel.sendParticles(BornInChaosV1ModParticleTypes.INFERNAL_SURGE.get(), x, y, z, 13, 2.0, 0.3, 2.0, 0.1);

        Vec3 center = new Vec3(x, y, z);
        List<LivingEntity> nearbyLiving = serverLevel.getEntitiesOfClass(LivingEntity.class, new AABB(center, center).inflate(3.5));
        MobEffect infernalFlame = BornInChaosV1ModMobEffects.INFERNAL_FLAME.get();
        for (LivingEntity target : nearbyLiving) {
            target.addEffect(new MobEffectInstance(infernalFlame, 240, 0));
        }

        removeIfPresent(living, MobEffects.MOVEMENT_SLOWDOWN);
        removeIfPresent(living, MobEffects.REGENERATION);
        removeIfPresent(living, MobEffects.MOVEMENT_SPEED);
        removeIfPresent(living, MobEffects.DAMAGE_RESISTANCE);
        removeIfPresent(living, MobEffects.ABSORPTION);
    }

    private static void removeIfPresent(LivingEntity living, MobEffect effect) {
        if (living.hasEffect(effect)) {
            living.removeEffect(effect);
        }
    }
}
