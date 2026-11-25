package com.hp.borninchaosoptimizer.mixin.scoreboard;

import com.hp.borninchaosoptimizer.config.BicOptimizerConfig;
import net.mcreator.borninchaosv.entity.SkeletonThrasherEntity;
import net.mcreator.borninchaosv.entity.SkeletonThrasherNotDespawnEntity;
import net.mcreator.borninchaosv.init.BornInChaosV1ModMobEffects;
import net.mcreator.borninchaosv.procedures.SheldBlocProcedure;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.registries.ForgeRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 替换 SheldBlocProcedure 中的原版 Scoreboard("blocc") 实体 UUID 写入为实体自身 PersistentData，
 * 杜绝 scoreboard.dat 永久膨胀并缓存音效查询。
 */
@Mixin(value = SheldBlocProcedure.class, remap = false)
public abstract class SheldBlocProcedureMixin {
    @Unique
    private static final String BIC_KEY_BLOCC = "bic_opt_blocc";
    @Unique
    private static final ResourceLocation BIC_SND_1 = ResourceLocation.tryBuild("born_in_chaos_v1", "skeleton_trasher_block1");
    @Unique
    private static final ResourceLocation BIC_SND_2 = ResourceLocation.tryBuild("born_in_chaos_v1", "skeleton_trasher_block2");
    @Unique
    private static final ResourceLocation BIC_SND_3 = ResourceLocation.tryBuild("born_in_chaos_v1", "skeleton_trasher_block3");

    @Inject(method = "execute", at = @At("HEAD"), cancellable = true)
    private static void bic$replaceScoreboardWithPersistentData(
            LevelAccessor world, double x, double y, double z, Entity entity, Entity sourceentity, CallbackInfo ci
    ) {
        if (!BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.enableGlobalFixes)
                || !BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.fixScoreboardEntityLeak)) {
            return;
        }
        ci.cancel();
        if (entity == null || sourceentity == null || world.isClientSide()) {
            return;
        }
        if (!(entity instanceof SkeletonThrasherEntity || entity instanceof SkeletonThrasherNotDespawnEntity)) {
            return;
        }
        if (!(sourceentity instanceof Player || sourceentity instanceof Monster || sourceentity instanceof Mob)) {
            return;
        }
        if (entity.isOnFire() || !(entity instanceof LivingEntity living)) {
            return;
        }
        if (living.getHealth() < 5.0f || entity.isInWall()
                || world.getBlockState(BlockPos.containing(x, y - 1.0, z)).getBlock() == Blocks.MAGMA_BLOCK) {
            return;
        }
        boolean hasBlockBreak = living.hasEffect(BornInChaosV1ModMobEffects.BLOCK_BREAK.get());
        if (!hasBlockBreak && !living.hasEffect(MobEffects.DAMAGE_RESISTANCE)) {
            living.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 20, 3, false, false));
            living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 10, 0, false, false));
        }
        if (living.hasEffect(MobEffects.DAMAGE_RESISTANCE) && !hasBlockBreak && world instanceof Level level) {
            ResourceLocation soundId;
            if (Math.random() < 0.45) {
                soundId = BIC_SND_1;
            } else if (Math.random() < 0.45) {
                soundId = BIC_SND_2;
            } else {
                soundId = BIC_SND_3;
            }
            SoundEvent sound = ForgeRegistries.SOUND_EVENTS.getValue(soundId);
            if (sound != null) {
                level.playSound(null, BlockPos.containing(x, y, z), sound, SoundSource.NEUTRAL, 0.6f, 1.0f);
            }
        }
        entity.getPersistentData().putBoolean(BIC_KEY_BLOCC, true);
    }
}
