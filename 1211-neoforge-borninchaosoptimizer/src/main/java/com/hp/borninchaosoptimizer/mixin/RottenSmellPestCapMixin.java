package com.hp.borninchaosoptimizer.mixin;

import com.hp.borninchaosoptimizer.config.OptimizerConfig;
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
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 为腐臭（Rotten Smell）效果滋生尸蝇与腐尸鱼增加局部密度上限保护：
 * Spark 实测 30 只感染怪物可在短时间内链式繁殖 343 只尸蝇导致客户端帧率从 395 暴跌至 38.9；
 * 当半径 16 格内同类害虫数量达到配置阈值时暂停继续孵化，完全保留正常战斗下的刷虫体验。
 */
@Mixin(value = RottenSmellKazhdyiTikVoVriemiaEffiektaProcedure.class, remap = false)
public abstract class RottenSmellPestCapMixin {
    @Unique
    private static final double[][] FLY_OFFSETS = {
            {2.5, 0.5}, {-2.5, 0.5}, {0.5, 2.5}, {0.5, -2.5}
    };

    @Unique
    private static final double[][] FISH_OFFSETS = {
            {4.5, 0.5}, {-4.5, 0.5}, {0.5, 4.5}, {0.5, -4.5}
    };

    @Inject(method = "execute", at = @At("HEAD"), cancellable = true)
    private static void bic$capRottenSmellPestSpawning(LevelAccessor world, double x, double y, double z, Entity entity, CallbackInfo ci) {
        if (!OptimizerConfig.isEnabled(OptimizerConfig.CAP_SUMMONED_PEST_DENSITY)) {
            return;
        }
        ci.cancel();
        if (entity == null || world.isClientSide() || !(world instanceof ServerLevel serverLevel)) {
            return;
        }
        boolean hasInsectProtection = entity instanceof LivingEntity living
                && living.hasEffect(BornInChaosV1ModMobEffects.INSECT_PROTECTION);
        int particleInterval = Math.max(1, OptimizerConfig.getInt(OptimizerConfig.ROTTEN_SMELL_PARTICLE_INTERVAL));
        if (!entity.isInWater()
                && !hasInsectProtection
                && ((entity.tickCount + entity.getId()) % particleInterval == 0)) {
            serverLevel.sendParticles(BornInChaosV1ModParticleTypes.FLI.get(), x, y + 1.0, z, 1, 0.3, 0.2, 0.3, 0.1);
        }

        double flyTimer = entity.getPersistentData().getDouble("fly");
        flyTimer = (flyTimer <= 0.0) ? 400.0 : (flyTimer - 1.0);
        entity.getPersistentData().putDouble("fly", flyTimer);
        if (flyTimer != 0.0) {
            return;
        }

        int maxNearby = OptimizerConfig.getInt(OptimizerConfig.MAX_NEARBY_PEST_COUNT);
        AABB densityBox = entity.getBoundingBox().inflate(16.0);

        if (!entity.isInWaterRainOrBubble() && !hasInsectProtection) {
            if (serverLevel.getEntitiesOfClass(CorpseFlyEntity.class, densityBox).size() >= maxNearby) {
                return;
            }
            for (double[] off : FLY_OFFSETS) {
                double sx = x + off[0];
                double sz = z + off[1];
                BlockPos lower = BlockPos.containing(sx, y, sz);
                BlockPos upper = BlockPos.containing(sx, y + 1.0, sz);
                if (!world.getBlockState(lower).canOcclude() && !world.getBlockState(upper).canOcclude()) {
                    BornInChaosV1ModEntities.CORPSE_FLY.get().spawn(serverLevel, lower, MobSpawnType.MOB_SUMMONED);
                    serverLevel.sendParticles(BornInChaosV1ModParticleTypes.FLI.get(), sx, y + 1.0, sz, 4, 0.3, 0.2, 0.3, 0.1);
                    serverLevel.sendParticles(ParticleTypes.POOF, sx, y + 1.0, sz, 4, 0.2, 0.2, 0.2, 0.1);
                    break;
                }
            }
        } else if (entity.isInWater()) {
            if (serverLevel.getEntitiesOfClass(CorpseFishEntity.class, densityBox).size() >= maxNearby) {
                return;
            }
            for (double[] off : FISH_OFFSETS) {
                double sx = x + off[0];
                double sz = z + off[1];
                BlockPos spawnPos = BlockPos.containing(sx, y, sz);
                if (!world.getBlockState(spawnPos).canOcclude()) {
                    BornInChaosV1ModEntities.CORPSE_FISH.get().spawn(serverLevel, spawnPos, MobSpawnType.MOB_SUMMONED);
                    serverLevel.sendParticles(ParticleTypes.POOF, sx, y, sz, 4, 0.2, 0.2, 0.2, 0.1);
                    serverLevel.sendParticles(ParticleTypes.BUBBLE_POP, sx, y, sz, 4, 0.3, 0.2, 0.3, 0.1);
                    break;
                }
            }
        }
    }
}
