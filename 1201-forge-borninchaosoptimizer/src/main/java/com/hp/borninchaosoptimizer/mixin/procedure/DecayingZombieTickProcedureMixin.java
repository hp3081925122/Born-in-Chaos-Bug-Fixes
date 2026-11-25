package com.hp.borninchaosoptimizer.mixin.procedure;

import com.hp.borninchaosoptimizer.config.BicOptimizerConfig;
import net.mcreator.borninchaosv.init.BornInChaosV1ModEntities;
import net.mcreator.borninchaosv.init.BornInChaosV1ModMobEffects;
import net.mcreator.borninchaosv.init.BornInChaosV1ModParticleTypes;
import net.mcreator.borninchaosv.procedures.DecayingZombiePriObnovlieniiTaktaSushchnostiProcedure;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.LevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 修复腐朽僵尸每 tick 过程的三个问题：
 * 1. 残血化为衰老骷髅与苍蝇寄生化为虫群怪缺少互斥 return 与幂等锁，同时满足时会一尸两命生成两只怪；
 * 2. 客户端无效空跑每 tick 天空遮挡查询；
 * 3. 白天自燃未先做昼夜与天气短路。
 */
@Mixin(value = DecayingZombiePriObnovlieniiTaktaSushchnostiProcedure.class, remap = false)
public abstract class DecayingZombieTickProcedureMixin {
    @Unique
    private static final String BIC_OPT$TRANSFORM_LOCK = "bic_opt_decaying_transformed";
    @Unique
    private static final ResourceLocation BIC_OPT$STOMACH_OPEN_SOUND =
            ResourceLocation.tryBuild("born_in_chaos_v1", "stomach_open");
    @Unique
    private static final ResourceLocation BIC_OPT$SLIME_BREAK_SOUND =
            ResourceLocation.tryBuild("minecraft", "block.slime_block.break");
    @Unique
    private static final ResourceLocation BIC_OPT$CONVERT_SOUND =
            ResourceLocation.tryBuild("minecraft", "entity.zombie.converted_to_drowned");

    @Inject(method = "execute", at = @At("HEAD"), cancellable = true)
    private static void bicOpt$safeTick(LevelAccessor world, double x, double y, double z, Entity entity, CallbackInfo ci) {
        if (!BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.enableGlobalFixes)
                || !BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.fixBossTransformAndTeleportBugs)) {
            return;
        }
        ci.cancel();
        if (!(entity instanceof LivingEntity living) || world.isClientSide() || !(world instanceof ServerLevel serverLevel)) {
            return;
        }
        if (entity.isRemoved() || entity.getPersistentData().getBoolean(BIC_OPT$TRANSFORM_LOCK)) {
            return;
        }

        BlockPos pos = BlockPos.containing(x, y, z);
        if (living.getHealth() <= 5.0f) {
            entity.getPersistentData().putBoolean(BIC_OPT$TRANSFORM_LOCK, true);
            serverLevel.sendParticles(BornInChaosV1ModParticleTypes.SPLASHOFFLESH.get(), x, y + 0.5, z, 15, 0.5, 0.5, 0.5, 0.2);
            serverLevel.sendParticles(BornInChaosV1ModParticleTypes.FLESHSPLASH.get(), x, y + 1.0, z, 5, 0.3, 0.3, 0.3, 0.1);
            Entity spawned = BornInChaosV1ModEntities.DECREPIT_SKELETON.get()
                    .spawn(serverLevel, pos, MobSpawnType.MOB_SUMMONED);
            if (spawned != null) {
                spawned.setYRot(entity.getYRot());
                spawned.setYBodyRot(entity.getYRot());
                spawned.setYHeadRot(entity.getYRot());
                spawned.setXRot(entity.getXRot());
            }
            entity.discard();
            for (int i = 0; i < 2; i++) {
                ItemEntity flesh = new ItemEntity(serverLevel, x, y, z, new ItemStack(Items.ROTTEN_FLESH));
                flesh.setPickUpDelay(10);
                serverLevel.addFreshEntity(flesh);
            }
            bicOpt$playSound(serverLevel, pos, BIC_OPT$STOMACH_OPEN_SOUND, SoundSource.HOSTILE, 0.7f, 1.0f);
            bicOpt$playSound(serverLevel, pos, BIC_OPT$SLIME_BREAK_SOUND, SoundSource.NEUTRAL, 0.9f, 0.9f);
            return;
        }

        MobEffect infestation = BornInChaosV1ModMobEffects.INFESTATIONOF_FLIES.get();
        MobEffectInstance effectInst = living.getEffect(infestation);
        if (effectInst != null && effectInst.getDuration() <= 20) {
            entity.getPersistentData().putBoolean(BIC_OPT$TRANSFORM_LOCK, true);
            Entity spawned = BornInChaosV1ModEntities.SWARMER.get()
                    .spawn(serverLevel, pos, MobSpawnType.MOB_SUMMONED);
            if (spawned != null) {
                spawned.setYRot(entity.getYRot());
                spawned.setYBodyRot(entity.getYRot());
                spawned.setYHeadRot(entity.getYRot());
                spawned.setXRot(entity.getXRot());
            }
            entity.discard();
            bicOpt$playSound(serverLevel, pos, BIC_OPT$CONVERT_SOUND, SoundSource.NEUTRAL, 0.8f, 0.9f);
            serverLevel.sendParticles(BornInChaosV1ModParticleTypes.FLI.get(), x, y + 1.0, z, 14, 0.5, 0.5, 0.5, 0.2);
            return;
        }

        if (entity.isOnFire() && entity.isInWaterRainOrBubble()) {
            if (serverLevel.canSeeSkyFromBelowWater(BlockPos.containing(x, y + 1.0, z))) {
                entity.clearFire();
            }
            return;
        }
        int interval = BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.optimizeSunAndSkyTickProcedures)
                ? Math.max(1, BicOptimizerConfig.getInt(BicOptimizerConfig.COMMON.sunCheckInterval))
                : 1;
        if (serverLevel.isDay()
                && !entity.isOnFire()
                && !entity.isInWaterRainOrBubble()
                && !serverLevel.getLevelData().isRaining()
                && !serverLevel.getLevelData().isThundering()
                && (entity.tickCount + entity.getId()) % interval == 0
                && serverLevel.canSeeSkyFromBelowWater(BlockPos.containing(x, y + 1.0, z))) {
            entity.setSecondsOnFire(5);
        }
    }

    @Unique
    private static void bicOpt$playSound(ServerLevel level, BlockPos pos, ResourceLocation id, SoundSource source, float vol, float pitch) {
        if (id == null) {
            return;
        }
        SoundEvent sound = net.minecraftforge.registries.ForgeRegistries.SOUND_EVENTS.getValue(id);
        if (sound != null) {
            level.playSound(null, pos, sound, source, vol, pitch);
        }
    }
}
