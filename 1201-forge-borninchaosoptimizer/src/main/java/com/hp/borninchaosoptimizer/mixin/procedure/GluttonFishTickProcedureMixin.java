package com.hp.borninchaosoptimizer.mixin.procedure;

import com.hp.borninchaosoptimizer.config.BicOptimizerConfig;
import com.hp.borninchaosoptimizer.util.BossTeleportHelper;
import net.mcreator.borninchaosv.init.BornInChaosV1ModMobEffects;
import net.mcreator.borninchaosv.init.BornInChaosV1ModParticleTypes;
import net.mcreator.borninchaosv.procedures.GluttonFishLandProcedure;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 修复暴食鱼每 tick 过程：仅服务端执行下潜瞬移，并在水中先检查 hasEffect 再移除凋零与鱼鳃呼吸，避免事件总线风暴。
 */
@Mixin(value = GluttonFishLandProcedure.class, remap = false)
public abstract class GluttonFishTickProcedureMixin {
    @Inject(method = "execute", at = @At("HEAD"), cancellable = true)
    private static void bicOpt$safeTick(LevelAccessor world, double x, double y, double z, Entity entity, CallbackInfo ci) {
        if (!BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.enableGlobalFixes)
                || !BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.fixEntityTickClientSideMutations)) {
            return;
        }
        ci.cancel();
        if (!(entity instanceof LivingEntity living) || world.isClientSide()) {
            return;
        }

        if (entity.isInWater() && world.getBlockState(BlockPos.containing(x, y + 1.0, z)) == Blocks.AIR.defaultBlockState()) {
            BossTeleportHelper.teleportEntity(entity, x, y - 2.0, z);
        }

        MobEffect fishBreath = BornInChaosV1ModMobEffects.FISH_BREATH.get();
        if (!entity.isInWater() && !living.hasEffect(fishBreath)) {
            living.addEffect(new MobEffectInstance(fishBreath, 600, 0, false, false));
        } else if (entity.isInWater()) {
            if (living.hasEffect(MobEffects.WITHER)) {
                living.removeEffect(MobEffects.WITHER);
            }
            if (living.hasEffect(fishBreath)) {
                living.removeEffect(fishBreath);
            }
            MobEffectInstance grace = living.getEffect(MobEffects.DOLPHINS_GRACE);
            if (!BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.optimizeSubEffectRefreshes)
                    || grace == null
                    || grace.getDuration() <= 10) {
                living.addEffect(new MobEffectInstance(MobEffects.DOLPHINS_GRACE, 40, 0, false, false));
            }
            if (living.hasEffect(BornInChaosV1ModMobEffects.STUN.get()) && world instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(BornInChaosV1ModParticleTypes.STUNSTARS.get(), x, y, z, 2, 1.3, 1.3, 1.3, 0.1);
            }
        }

        MobEffectInstance breathInst = living.getEffect(fishBreath);
        if (breathInst != null && breathInst.getDuration() == 20) {
            living.addEffect(new MobEffectInstance(MobEffects.WITHER, 1000, 1, false, false));
        }
    }
}
