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
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 优化连体骷髅每刻过程：仅服务端权威执行（避免客户端未销毁前每刻连播受伤音效与空跑日光扫描），增加一次性分裂幂等锁，并前置阳光自燃常数短路检查。
 */
@Mixin(value = SiameseskeletonsticProcedure.class, remap = false)
public abstract class SiameseSkeletonsTickOptimizeMixin {
    @Unique
    private static final String BIC_TRANSFORMED_KEY = "bic_phase_transformed";

    @Inject(method = "execute", at = @At("HEAD"), cancellable = true)
    private static void bic$optimizeSiameseSkeletonTick(LevelAccessor world, double x, double y, double z, Entity entity, CallbackInfo ci) {
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
        if (living.getHealth() <= 10.0f) {
            entity.getPersistentData().putBoolean(BIC_TRANSFORMED_KEY, true);
            BlockPos pos = BlockPos.containing(x, y, z);
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
            serverLevel.playSound(null, pos, SoundEvents.SKELETON_HURT, SoundSource.NEUTRAL, 1.0f, 1.0f);
            entity.discard();
            return;
        }
        EntityEffectHelper.handleUndeadSunburn(world, x, y, z, entity);
    }
}
