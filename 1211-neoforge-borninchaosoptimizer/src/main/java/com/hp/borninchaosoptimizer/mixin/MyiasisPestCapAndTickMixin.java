package com.hp.borninchaosoptimizer.mixin;

import com.hp.borninchaosoptimizer.config.OptimizerConfig;
import com.hp.borninchaosoptimizer.util.EntityEffectHelper;
import net.mcreator.borninchaosv.entity.MaggotEntity;
import net.mcreator.borninchaosv.init.BornInChaosV1ModEntities;
import net.mcreator.borninchaosv.init.BornInChaosV1ModMobEffects;
import net.mcreator.borninchaosv.init.BornInChaosV1ModParticleTypes;
import net.mcreator.borninchaosv.procedures.InsipidityKazhdyiTikVoVriemiaEffiektaProcedure;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.LevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 优化蝇蛆病（Myiasis / Insipidity）每刻过程：
 * 1. 仅在持有生命恢复效果时才移除，消除每刻无意义的 MobEffectEvent.Remove 广播；
 * 2. 遇水驱散后立即返回，不再误判为剩余时长 <= 20 而错误刷出蛆虫（修复入水洗掉蝇蛆病反而立刻刷出蛆虫的原版逻辑漏洞！）；
 * 3. 对孵化蛆虫同样施加局部密度上限保护。
 */
@Mixin(value = InsipidityKazhdyiTikVoVriemiaEffiektaProcedure.class, remap = false)
public abstract class MyiasisPestCapAndTickMixin {
    @Inject(method = "execute", at = @At("HEAD"), cancellable = true)
    private static void bic$optimizeMyiasisAndCapMaggots(LevelAccessor world, double x, double y, double z, Entity entity, CallbackInfo ci) {
        if (!OptimizerConfig.isEnabled(OptimizerConfig.CAP_SUMMONED_PEST_DENSITY)) {
            return;
        }
        ci.cancel();
        if (!(entity instanceof LivingEntity living)) {
            return;
        }
        EntityEffectHelper.removeEffectIfPresent(living, MobEffects.REGENERATION);
        if (entity.isInWaterOrBubble()) {
            EntityEffectHelper.removeEffectIfPresent(living, BornInChaosV1ModMobEffects.MYIASIS);
            return;
        }
        MobEffectInstance myiasis = living.getEffect(BornInChaosV1ModMobEffects.MYIASIS);
        if (myiasis != null && myiasis.getDuration() <= 20) {
            living.removeEffect(BornInChaosV1ModMobEffects.MYIASIS);
            if (world instanceof ServerLevel serverLevel) {
                int maxNearby = OptimizerConfig.getInt(OptimizerConfig.MAX_NEARBY_PEST_COUNT);
                if (serverLevel.getEntitiesOfClass(MaggotEntity.class, entity.getBoundingBox().inflate(16.0)).size() < maxNearby) {
                    Entity spawned = BornInChaosV1ModEntities.MAGGOT.get().spawn(
                            serverLevel,
                            BlockPos.containing(x + 0.5, y + 1.0, z + 0.5),
                            MobSpawnType.MOB_SUMMONED
                    );
                    if (spawned != null) {
                        spawned.setYRot(world.getRandom().nextFloat() * 360.0f);
                    }
                    serverLevel.sendParticles(BornInChaosV1ModParticleTypes.FLESHSPLASH.get(), x + 0.5, y + 1.0, z + 0.5, 3, 0.2, 0.2, 0.2, 0.1);
                }
            }
        }
    }
}
