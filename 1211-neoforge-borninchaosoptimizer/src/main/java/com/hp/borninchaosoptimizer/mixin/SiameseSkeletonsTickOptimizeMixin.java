package com.hp.borninchaosoptimizer.mixin;

import com.hp.borninchaosoptimizer.config.OptimizerConfig;
import com.hp.borninchaosoptimizer.util.EntityEffectHelper;
import net.mcreator.borninchaosv.init.BornInChaosV1ModEntities;
import net.mcreator.borninchaosv.procedures.SiameseskeletonsticProcedure;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 优化连体骷髅每刻过程：前置阳光自燃常数短路检查，并在分裂销毁后直接返回避免多余的天空方块柱扫描。
 */
@Mixin(value = SiameseskeletonsticProcedure.class, remap = false)
public abstract class SiameseSkeletonsTickOptimizeMixin {
    @Inject(method = "execute", at = @At("HEAD"), cancellable = true)
    private static void bic$optimizeSiameseSkeletonTick(LevelAccessor world, double x, double y, double z, Entity entity, CallbackInfo ci) {
        if (!OptimizerConfig.isEnabled(OptimizerConfig.OPTIMIZE_SKY_AND_SUNBURN_CHECKS)) {
            return;
        }
        ci.cancel();
        if (!(entity instanceof LivingEntity living)) {
            return;
        }
        if (living.getHealth() <= 10.0f) {
            BlockPos pos = BlockPos.containing(x, y, z);
            if (world instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.POOF, x, y, z, 6, 0.2, 0.2, 0.2, 0.1);
                Entity right = BornInChaosV1ModEntities.SIAMESE_SKELETONSRIGHT.get().spawn(serverLevel, pos, MobSpawnType.MOB_SUMMONED);
                if (right != null) {
                    right.setYRot(entity.getYRot());
                    right.setYBodyRot(entity.getYRot());
                    right.setYHeadRot(entity.getYRot());
                    right.setXRot(entity.getXRot());
                }
                Entity left = BornInChaosV1ModEntities.SIAMESE_SKELETONSLEFT.get().spawn(serverLevel, pos, MobSpawnType.MOB_SUMMONED);
                if (left != null) {
                    left.setYRot(entity.getYRot());
                    left.setYBodyRot(entity.getYRot());
                    left.setYHeadRot(entity.getYRot());
                    left.setXRot(entity.getXRot());
                }
            }
            if (world instanceof Level level) {
                if (!level.isClientSide()) {
                    level.playSound(null, pos, SoundEvents.SKELETON_HURT, SoundSource.NEUTRAL, 1.0f, 1.0f);
                } else {
                    level.playLocalSound(x, y, z, SoundEvents.SKELETON_HURT, SoundSource.NEUTRAL, 1.0f, 1.0f, false);
                }
            }
            if (!entity.level().isClientSide()) {
                entity.discard();
            }
            return;
        }
        EntityEffectHelper.handleUndeadSunburn(world, x, y, z, entity);
    }
}
