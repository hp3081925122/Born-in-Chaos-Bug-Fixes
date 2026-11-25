package com.hp.borninchaosoptimizer.mixin;

import com.hp.borninchaosoptimizer.config.OptimizerConfig;
import java.util.List;
import net.mcreator.borninchaosv.entity.KrampusEntity;
import net.mcreator.borninchaosv.entity.KrampusHenchmanEntity;
import net.mcreator.borninchaosv.init.BornInChaosV1ModMobEffects;
import net.mcreator.borninchaosv.init.BornInChaosV1ModParticleTypes;
import net.mcreator.borninchaosv.procedures.SnowStormKazhdyiTikVoVriemiaEffiektaProcedure;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 优化暴风雪（Snow Storm）每刻过程：
 * 1. 直接查询 LivingEntity.class 替代 Entity.class + 流式排序，防止冰冻光环误毁展示框、画、经验球与矿车；
 * 2. 将 8 层重复的寒骨（BONE_CHILLING）查询合并为单次读取，并合并玩家踏水结冰的 4 次方块读取。
 */
@Mixin(value = SnowStormKazhdyiTikVoVriemiaEffiektaProcedure.class, remap = false)
public abstract class SnowStormOptimizeMixin {
    @Inject(method = "execute", at = @At("HEAD"), cancellable = true)
    private static void bic$optimizeSnowStormTick(LevelAccessor world, double x, double y, double z, Entity entity, CallbackInfo ci) {
        if (!OptimizerConfig.isEnabled(OptimizerConfig.OPTIMIZE_EFFECT_TICK_SCANS)) {
            return;
        }
        ci.cancel();
        if (entity == null) {
            return;
        }
        if (world instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(BornInChaosV1ModParticleTypes.SNOWCLOUD.get(), entity.getX(), entity.getY() + 0.2, entity.getZ(), 3, 1.4, 0.1, 1.4, 0.1);
            serverLevel.sendParticles(BornInChaosV1ModParticleTypes.LITTLESNOWFLAKE.get(), entity.getX(), entity.getY() + 2.0, entity.getZ(), 3, 1.0, 0.6, 1.0, 0.2);
            serverLevel.sendParticles(BornInChaosV1ModParticleTypes.WANINGSNOWFLAKE.get(), entity.getX(), entity.getY() + 2.0, entity.getZ(), 1, 1.0, 0.6, 1.0, 0.1);
        }

        double freezing = entity.getPersistentData().getDouble("freezing");
        freezing = (freezing == 0.0) ? 40.0 : (freezing - 1.0);
        entity.getPersistentData().putDouble("freezing", freezing);

        if (freezing == 0.0) {
            AABB box = new AABB(
                    entity.getX() - 3.75, entity.getY() - 3.75, entity.getZ() - 3.75,
                    entity.getX() + 3.75, entity.getY() + 3.75, entity.getZ() + 3.75
            );
            DamageSource freezeSource = new DamageSource(world.holderOrThrow(DamageTypes.FREEZE));
            LivingEntity ownerLiving = (entity instanceof LivingEntity liv) ? liv : null;
            List<LivingEntity> targets = world.getEntitiesOfClass(LivingEntity.class, box, candidate -> {
                if (candidate == entity || candidate instanceof KrampusEntity || candidate instanceof KrampusHenchmanEntity) {
                    return false;
                }
                if (candidate.hasEffect(BornInChaosV1ModMobEffects.SNOW_STORM)) {
                    return false;
                }
                return !(candidate instanceof TamableAnimal tamable && ownerLiving != null && tamable.isOwnedBy(ownerLiving));
            });

            for (LivingEntity target : targets) {
                MobEffectInstance currentChill = target.getEffect(BornInChaosV1ModMobEffects.BONE_CHILLING);
                int nextAmp;
                int nextDur;
                float damage;
                if (currentChill == null) {
                    nextAmp = 0;
                    nextDur = 80;
                    damage = 1.0f;
                } else {
                    int amp = currentChill.getAmplifier();
                    switch (amp) {
                        case 0 -> { nextAmp = 1; nextDur = 100; damage = 1.0f; }
                        case 1 -> { nextAmp = 2; nextDur = 200; damage = 1.0f; }
                        case 2 -> { nextAmp = 3; nextDur = 140; damage = 2.0f; }
                        case 3 -> { nextAmp = 4; nextDur = 100; damage = 2.0f; }
                        case 4 -> { nextAmp = 5; nextDur = 100; damage = 3.0f; }
                        case 5 -> { nextAmp = 6; nextDur = 80; damage = 3.0f; }
                        default -> { nextAmp = 6; nextDur = 80; damage = 4.0f; }
                    }
                }
                if (!target.level().isClientSide()) {
                    target.addEffect(new MobEffectInstance(BornInChaosV1ModMobEffects.BONE_CHILLING, nextDur, nextAmp));
                }
                target.hurt(freezeSource, damage);
                if (world instanceof Level level && !level.isClientSide()) {
                    level.playSound(null, BlockPos.containing(target.getX(), target.getY() + 1.4, target.getZ()), SoundEvents.PLAYER_HURT_FREEZE, SoundSource.NEUTRAL, 0.8f, 1.0f);
                }
                if (world instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(BornInChaosV1ModParticleTypes.LITTLESNOWFLAKE.get(), target.getX(), target.getY() + 1.4, target.getZ(), 4, 0.25, 0.25, 0.25, 0.1);
                    serverLevel.sendParticles(BornInChaosV1ModParticleTypes.WANINGSNOWFLAKE.get(), target.getX(), target.getY() + 1.4, target.getZ(), 2, 0.25, 0.25, 0.25, 0.1);
                }
            }
        }

        if (entity instanceof Player && !world.isClientSide()) {
            BlockPos belowPos = BlockPos.containing(x, y - 1.0, z);
            Block below = world.getBlockState(belowPos).getBlock();
            if (below == Blocks.WATER || below == Blocks.KELP_PLANT || below == Blocks.SEAGRASS || below == Blocks.TALL_SEAGRASS) {
                world.setBlock(belowPos, Blocks.FROSTED_ICE.defaultBlockState(), 3);
            }
        }
    }
}
