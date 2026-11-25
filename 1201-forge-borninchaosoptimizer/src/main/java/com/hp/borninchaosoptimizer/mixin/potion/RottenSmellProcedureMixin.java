package com.hp.borninchaosoptimizer.mixin.potion;

import com.hp.borninchaosoptimizer.config.BicOptimizerConfig;
import net.mcreator.borninchaosv.entity.CorpseFishEntity;
import net.mcreator.borninchaosv.entity.CorpseFlyEntity;
import net.mcreator.borninchaosv.init.BornInChaosV1ModEntities;
import net.mcreator.borninchaosv.init.BornInChaosV1ModMobEffects;
import net.mcreator.borninchaosv.init.BornInChaosV1ModParticleTypes;
import net.mcreator.borninchaosv.procedures.RottenSmellKazhdyiTikVoVriemiaEffiektaProcedure;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.LevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 修复腐臭气味（Rotten Smell）导致尸蝇无限繁殖爆炸（Spark 实测 30 只僵尸膨胀至 374 实体、FPS 暴跌 90%）、
 * 每 tick 高频粒子网络发包以及客户端篡改 PersistentData 的问题。
 */
@Mixin(value = RottenSmellKazhdyiTikVoVriemiaEffiektaProcedure.class, remap = false)
public abstract class RottenSmellProcedureMixin {
    private static final double[][] FLY_OFFSETS = {
            {2.5, 0.5},
            {-2.5, 0.5},
            {0.5, 2.5},
            {0.5, -2.5}
    };

    private static final double[][] FISH_OFFSETS = {
            {4.5, 0.5},
            {-4.5, 0.5},
            {0.5, 4.5},
            {0.5, -4.5}
    };

    @Inject(method = "execute", at = @At("HEAD"), cancellable = true)
    private static void bicOptimizer$optimizeRottenSmell(LevelAccessor world, double x, double y, double z, Entity entity, CallbackInfo ci) {
        if (!BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.enableGlobalFixes)
                || !BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.optimizeRottenSmellEffect)) {
            return;
        }
        ci.cancel();
        if (entity == null || !(world instanceof ServerLevel serverLevel)) {
            return;
        }

        boolean hasInsectProtection = entity instanceof LivingEntity living
                && living.hasEffect(BornInChaosV1ModMobEffects.INSECT_PROTECTION.get());

        int particleInterval = Math.max(1, BicOptimizerConfig.getInt(BicOptimizerConfig.COMMON.rottenSmellParticleInterval));
        if (!entity.isInWater() && !hasInsectProtection && (entity.tickCount + entity.getId()) % particleInterval == 0) {
            serverLevel.sendParticles(BornInChaosV1ModParticleTypes.FLI.get(), x, y + 1.0, z, 1, 0.3, 0.2, 0.3, 0.1);
        }

        double flyTimer = entity.getPersistentData().getDouble("fly");
        if (flyTimer <= 0.0) {
            flyTimer = 400.0;
        } else {
            flyTimer -= 1.0;
        }
        entity.getPersistentData().putDouble("fly", flyTimer);

        if (flyTimer != 0.0) {
            return;
        }

        int maxNearby = BicOptimizerConfig.getInt(BicOptimizerConfig.COMMON.maxNearbyRottenSmellSummons);
        if (!entity.isInWaterRainOrBubble() && !hasInsectProtection) {
            if (maxNearby > 0 && serverLevel.getEntitiesOfClass(CorpseFlyEntity.class, entity.getBoundingBox().inflate(16.0)).size() >= maxNearby) {
                return;
            }
            for (double[] offset : FLY_OFFSETS) {
                double sx = x + offset[0];
                double sz = z + offset[1];
                BlockPos lower = BlockPos.containing(sx, y, sz);
                BlockPos upper = BlockPos.containing(sx, y + 1.0, sz);
                if (!serverLevel.getBlockState(lower).canOcclude() && !serverLevel.getBlockState(upper).canOcclude()) {
                    BornInChaosV1ModEntities.CORPSE_FLY.get().spawn(serverLevel, lower, MobSpawnType.MOB_SUMMONED);
                    serverLevel.sendParticles(BornInChaosV1ModParticleTypes.FLI.get(), sx, y + 1.0, sz, 4, 0.3, 0.2, 0.3, 0.1);
                    serverLevel.sendParticles(ParticleTypes.POOF, sx, y + 1.0, sz, 4, 0.2, 0.2, 0.2, 0.1);
                    break;
                }
            }
        } else if (entity.isInWater()) {
            if (maxNearby > 0 && serverLevel.getEntitiesOfClass(CorpseFishEntity.class, entity.getBoundingBox().inflate(16.0)).size() >= maxNearby) {
                return;
            }
            for (double[] offset : FISH_OFFSETS) {
                double sx = x + offset[0];
                double sz = z + offset[1];
                BlockPos spawnPos = BlockPos.containing(sx, y, sz);
                if (!serverLevel.getBlockState(spawnPos).canOcclude()) {
                    BornInChaosV1ModEntities.CORPSE_FISH.get().spawn(serverLevel, spawnPos, MobSpawnType.MOB_SUMMONED);
                    serverLevel.sendParticles(ParticleTypes.POOF, sx, y, sz, 4, 0.2, 0.2, 0.2, 0.1);
                    serverLevel.sendParticles(ParticleTypes.BUBBLE_POP, sx, y, sz, 4, 0.3, 0.2, 0.3, 0.1);
                    break;
                }
            }
        }
    }
}
