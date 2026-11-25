package com.hp.borninchaosoptimizer.mixin.procedure;

import com.hp.borninchaosoptimizer.config.BicOptimizerConfig;
import net.mcreator.borninchaosv.init.BornInChaosV1ModEntities;
import net.mcreator.borninchaosv.procedures.SiameseskeletonsticProcedure;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.LevelAccessor;
import net.minecraftforge.registries.ForgeRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 修复连体骷髅每 tick 分裂与自燃过程：增加服务端限定、分裂幂等锁及夜晚/雨天天空检测短路。
 */
@Mixin(value = SiameseskeletonsticProcedure.class, remap = false)
public abstract class SiameseSkeletonsTickProcedureMixin {
    @Unique
    private static final String BIC_OPT$SPLIT_LOCK = "bic_opt_siamese_split";
    @Unique
    private static final ResourceLocation BIC_OPT$SKELETON_HURT_SOUND =
            ResourceLocation.tryBuild("minecraft", "entity.skeleton.hurt");

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
        if (entity.isRemoved() || entity.getPersistentData().getBoolean(BIC_OPT$SPLIT_LOCK)) {
            return;
        }

        BlockPos pos = BlockPos.containing(x, y, z);
        if (living.getHealth() <= 10.0f) {
            entity.getPersistentData().putBoolean(BIC_OPT$SPLIT_LOCK, true);
            serverLevel.sendParticles(ParticleTypes.POOF, x, y, z, 6, 0.2, 0.2, 0.2, 0.1);
            if (BIC_OPT$SKELETON_HURT_SOUND != null) {
                SoundEvent sound = ForgeRegistries.SOUND_EVENTS.getValue(BIC_OPT$SKELETON_HURT_SOUND);
                if (sound != null) {
                    serverLevel.playSound(null, pos, sound, SoundSource.NEUTRAL, 1.0f, 1.0f);
                }
            }
            Entity right = BornInChaosV1ModEntities.SIAMESE_SKELETONSRIGHT.get()
                    .spawn(serverLevel, pos, MobSpawnType.MOB_SUMMONED);
            if (right != null) {
                right.setYRot(entity.getYRot());
                right.setYBodyRot(entity.getYRot());
                right.setYHeadRot(entity.getYRot());
                right.setXRot(entity.getXRot());
            }
            Entity left = BornInChaosV1ModEntities.SIAMESE_SKELETONSLEFT.get()
                    .spawn(serverLevel, pos, MobSpawnType.MOB_SUMMONED);
            if (left != null) {
                left.setYRot(entity.getYRot());
                left.setYBodyRot(entity.getYRot());
                left.setYHeadRot(entity.getYRot());
                left.setXRot(entity.getXRot());
            }
            entity.discard();
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
}
