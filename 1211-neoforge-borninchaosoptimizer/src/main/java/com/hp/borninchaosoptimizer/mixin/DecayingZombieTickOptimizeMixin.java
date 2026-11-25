package com.hp.borninchaosoptimizer.mixin;

import com.hp.borninchaosoptimizer.config.OptimizerConfig;
import com.hp.borninchaosoptimizer.util.EntityEffectHelper;
import net.mcreator.borninchaosv.init.BornInChaosV1ModEntities;
import net.mcreator.borninchaosv.init.BornInChaosV1ModMobEffects;
import net.mcreator.borninchaosv.init.BornInChaosV1ModParticleTypes;
import net.mcreator.borninchaosv.init.BornInChaosV1ModSounds;
import net.mcreator.borninchaosv.procedures.DecayingZombiePriObnovlieniiTaktaSushchnostiProcedure;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 优化腐烂僵尸每刻过程：仅服务端权威执行（避免客户端未销毁前每刻连播开肚音效与空跑日光扫描），前置阳光自燃常数短路，并加一次性转化幂等锁防止一尸两命或重复刷怪。
 */
@Mixin(value = DecayingZombiePriObnovlieniiTaktaSushchnostiProcedure.class, remap = false)
public abstract class DecayingZombieTickOptimizeMixin {
    @Unique
    private static final String BIC_TRANSFORMED_KEY = "bic_phase_transformed";

    @Inject(method = "execute", at = @At("HEAD"), cancellable = true)
    private static void bic$optimizeDecayingZombieTick(LevelAccessor world, double x, double y, double z, Entity entity, CallbackInfo ci) {
        if (!OptimizerConfig.isEnabled(OptimizerConfig.OPTIMIZE_SKY_AND_SUNBURN_CHECKS)
                && !OptimizerConfig.isEnabled(OptimizerConfig.FIX_BOSS_AND_MINION_DUPLICATE_SPAWN)) {
            return;
        }
        ci.cancel();
        if (!(entity instanceof LivingEntity living) || world.isClientSide() || !(world instanceof ServerLevel serverLevel)) {
            return;
        }
        if (entity.isRemoved() || entity.getPersistentData().getBoolean(BIC_TRANSFORMED_KEY)) {
            return;
        }
        if (living.getHealth() <= 5.0f) {
            entity.getPersistentData().putBoolean(BIC_TRANSFORMED_KEY, true);
            BlockPos pos = BlockPos.containing(x, y, z);
            serverLevel.sendParticles(BornInChaosV1ModParticleTypes.SPLASHOFFLESH.get(), x, y + 0.5, z, 15, 0.5, 0.5, 0.5, 0.2);
            serverLevel.sendParticles(BornInChaosV1ModParticleTypes.FLESHSPLASH.get(), x, y + 1.0, z, 5, 0.3, 0.3, 0.3, 0.1);
            Entity spawned = BornInChaosV1ModEntities.DECREPIT_SKELETON.get().spawn(serverLevel, pos, MobSpawnType.MOB_SUMMONED);
            if (spawned != null) {
                spawned.setYRot(entity.getYRot());
                spawned.setYBodyRot(entity.getYRot());
                spawned.setYHeadRot(entity.getYRot());
                spawned.setXRot(entity.getXRot());
            }
            for (int i = 0; i < 2; i++) {
                ItemEntity drop = new ItemEntity(serverLevel, x, y, z, new ItemStack(Items.ROTTEN_FLESH));
                drop.setPickUpDelay(10);
                serverLevel.addFreshEntity(drop);
            }
            entity.discard();
            serverLevel.playSound(null, pos, BornInChaosV1ModSounds.STOMACH_OPEN.get(), SoundSource.HOSTILE, 0.7f, 1.0f);
            serverLevel.playSound(null, pos, SoundEvents.SLIME_BLOCK_BREAK, SoundSource.NEUTRAL, 0.9f, 0.9f);
            return;
        }

        EntityEffectHelper.handleUndeadSunburn(world, x, y, z, entity);

        MobEffectInstance infestation = living.getEffect(BornInChaosV1ModMobEffects.INFESTATIONOF_FLIES);
        if (infestation != null && infestation.getDuration() <= 20) {
            entity.getPersistentData().putBoolean(BIC_TRANSFORMED_KEY, true);
            BlockPos pos = BlockPos.containing(x, y, z);
            Entity spawned = BornInChaosV1ModEntities.SWARMER.get().spawn(serverLevel, pos, MobSpawnType.MOB_SUMMONED);
            if (spawned != null) {
                spawned.setYRot(entity.getYRot());
                spawned.setYBodyRot(entity.getYRot());
                spawned.setYHeadRot(entity.getYRot());
                spawned.setXRot(entity.getXRot());
            }
            serverLevel.sendParticles(BornInChaosV1ModParticleTypes.FLI.get(), x, y + 1.0, z, 14, 0.5, 0.5, 0.5, 0.2);
            entity.discard();
            serverLevel.playSound(null, pos, SoundEvents.ZOMBIE_CONVERTED_TO_DROWNED, SoundSource.NEUTRAL, 0.8f, 0.9f);
        }
    }
}
