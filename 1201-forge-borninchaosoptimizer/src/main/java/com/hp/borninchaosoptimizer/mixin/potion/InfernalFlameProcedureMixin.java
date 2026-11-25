package com.hp.borninchaosoptimizer.mixin.potion;

import com.hp.borninchaosoptimizer.config.BicOptimizerConfig;
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
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.LevelAccessor;
import net.minecraftforge.registries.ForgeRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 修复地狱火（Infernal Flame）双端独立递减 infernal 计时器、客户端无效 hurt 与双重重叠音效问题，
 * 并简化冗余的多重血量读取分支。
 */
@Mixin(value = InfernalFlameKazhdyiTikVoVriemiaEffiektaProcedure.class, remap = false)
public abstract class InfernalFlameProcedureMixin {
    private static final ResourceLocation HURT_ON_FIRE_SOUND_ID = ResourceLocation.tryParse("minecraft:entity.player.hurt_on_fire");

    @Inject(method = "execute", at = @At("HEAD"), cancellable = true)
    private static void bicOptimizer$optimizeInfernalFlame(LevelAccessor world, double x, double y, double z, Entity entity, CallbackInfo ci) {
        if (!BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.enableGlobalFixes)
                || !BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.optimizePotionAuraAndStateTicks)) {
            return;
        }
        ci.cancel();
        if (entity == null || !(world instanceof ServerLevel serverLevel)) {
            return;
        }

        serverLevel.sendParticles(BornInChaosV1ModParticleTypes.ANIM_FIRE.get(), x, y + 1.0, z, 1, 0.3, 0.3, 0.3, 0.1);

        if (isInfernalImmuneEntity(entity) && entity instanceof LivingEntity living) {
            MobEffect infernalFlame = BornInChaosV1ModMobEffects.INFERNAL_FLAME.get();
            if (living.hasEffect(infernalFlame)) {
                living.removeEffect(infernalFlame);
            }
            return;
        }

        double timer = entity.getPersistentData().getDouble("infernal");
        if (timer <= 0.0) {
            timer = 40.0;
        } else {
            timer -= 1.0;
        }
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

        DamageSource fireDamage = new DamageSource(
                serverLevel.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(DamageTypes.IN_FIRE)
        );
        entity.hurt(fireDamage, damage);
        serverLevel.sendParticles(BornInChaosV1ModParticleTypes.INFERNAL_SURGE.get(), x, y + 1.0, z, 3, 0.3, 0.3, 0.3, 0.1);

        SoundEvent sound = HURT_ON_FIRE_SOUND_ID != null ? ForgeRegistries.SOUND_EVENTS.getValue(HURT_ON_FIRE_SOUND_ID) : null;
        if (sound != null) {
            serverLevel.playSound(null, BlockPos.containing(x, y, z), sound, SoundSource.NEUTRAL, 0.9f, 0.8f);
        }
    }

    private static boolean isInfernalImmuneEntity(Entity entity) {
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
