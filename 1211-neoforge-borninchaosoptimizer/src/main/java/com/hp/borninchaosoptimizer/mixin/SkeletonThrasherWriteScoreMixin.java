package com.hp.borninchaosoptimizer.mixin;

import com.hp.borninchaosoptimizer.config.OptimizerConfig;
import net.mcreator.borninchaosv.entity.SkeletonThrasherEntity;
import net.mcreator.borninchaosv.entity.SkeletonThrasherNotDespawnEntity;
import net.mcreator.borninchaosv.init.BornInChaosV1ModMobEffects;
import net.mcreator.borninchaosv.init.BornInChaosV1ModSounds;
import net.mcreator.borninchaosv.procedures.SheldBlocProcedure;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 修复碎骨骷髅受击格挡时向全局计分板写入永久实体 UUID 分数导致的存档膨胀与发包问题。
 */
@Mixin(value = SheldBlocProcedure.class, remap = false)
public abstract class SkeletonThrasherWriteScoreMixin {
    @Inject(method = "execute", at = @At("HEAD"), cancellable = true)
    private static void bic$replaceScoreboardWrite(LevelAccessor world, double x, double y, double z, Entity entity, Entity sourceentity, CallbackInfo ci) {
        if (!OptimizerConfig.isEnabled(OptimizerConfig.FIX_SCOREBOARD_LEAK)) {
            return;
        }
        ci.cancel();
        if (entity == null || sourceentity == null) {
            return;
        }
        if ((entity instanceof SkeletonThrasherEntity || entity instanceof SkeletonThrasherNotDespawnEntity)
                && (sourceentity instanceof Player || sourceentity instanceof Mob)
                && !entity.isOnFire()
                && entity instanceof LivingEntity living
                && living.getHealth() >= 5.0f
                && !entity.isInWall()
                && !world.getBlockState(BlockPos.containing(x, y - 1.0, z)).is(Blocks.MAGMA_BLOCK)) {
            if (!living.hasEffect(BornInChaosV1ModMobEffects.BLOCK_BREAK) && !living.hasEffect(MobEffects.DAMAGE_RESISTANCE)) {
                if (!living.level().isClientSide()) {
                    living.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 20, 3, false, false));
                    living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 10, 0, false, false));
                }
            }
            if (living.hasEffect(MobEffects.DAMAGE_RESISTANCE)
                    && !living.hasEffect(BornInChaosV1ModMobEffects.BLOCK_BREAK)
                    && !world.isClientSide()
                    && world instanceof Level level) {
                SoundEvent sound;
                if (Math.random() < 0.45) {
                    sound = BornInChaosV1ModSounds.SKELETON_TRASHER_BLOCK1.get();
                } else if (Math.random() < 0.45) {
                    sound = BornInChaosV1ModSounds.SKELETON_TRASHER_BLOCK2.get();
                } else {
                    sound = BornInChaosV1ModSounds.SKELETON_TRASHER_BLOCK3.get();
                }
                level.playSound(null, BlockPos.containing(x, y, z), sound, SoundSource.NEUTRAL, 0.6f, 1.0f);
            }
            entity.getPersistentData().putBoolean("bic_blocc", true);
        }
    }
}
