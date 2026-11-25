package com.hp.borninchaosoptimizer.mixin.potion;

import com.hp.borninchaosoptimizer.config.BicOptimizerConfig;
import net.mcreator.borninchaosv.entity.MaggotEntity;
import net.mcreator.borninchaosv.init.BornInChaosV1ModEntities;
import net.mcreator.borninchaosv.init.BornInChaosV1ModMobEffects;
import net.mcreator.borninchaosv.init.BornInChaosV1ModParticleTypes;
import net.mcreator.borninchaosv.procedures.InsipidityKazhdyiTikVoVriemiaEffiektaProcedure;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
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
 * 修复蝇蛆症（Myiasis）每 tick 在双端无条件调用 removeEffect 刷爆 Forge 事件总线的问题，
 * 并增加附近腐蛆实体密度上限防止群体感染时无上限刷怪。
 */
@Mixin(value = InsipidityKazhdyiTikVoVriemiaEffiektaProcedure.class, remap = false)
public abstract class MyiasisProcedureMixin {
    @Inject(method = "execute", at = @At("HEAD"), cancellable = true)
    private static void bicOptimizer$optimizeMyiasis(LevelAccessor world, double x, double y, double z, Entity entity, CallbackInfo ci) {
        if (!BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.enableGlobalFixes)
                || !BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.optimizeMyiasisEffect)) {
            return;
        }
        ci.cancel();
        if (!(entity instanceof LivingEntity living) || !(world instanceof ServerLevel serverLevel)) {
            return;
        }

        if (living.hasEffect(MobEffects.REGENERATION)) {
            living.removeEffect(MobEffects.REGENERATION);
        }

        MobEffect myiasis = BornInChaosV1ModMobEffects.MYIASIS.get();
        if (living.isInWaterOrBubble()) {
            if (living.hasEffect(myiasis)) {
                living.removeEffect(myiasis);
            }
            return;
        }

        MobEffectInstance effectInstance = living.getEffect(myiasis);
        int duration = effectInstance != null ? effectInstance.getDuration() : 0;
        if (duration <= 20) {
            if (effectInstance != null) {
                living.removeEffect(myiasis);
            }
            int maxMaggots = BicOptimizerConfig.getInt(BicOptimizerConfig.COMMON.maxNearbyMyiasisMaggots);
            if (maxMaggots <= 0 || serverLevel.getEntitiesOfClass(MaggotEntity.class, living.getBoundingBox().inflate(16.0)).size() < maxMaggots) {
                Entity spawned = BornInChaosV1ModEntities.MAGGOT.get().spawn(
                        serverLevel,
                        BlockPos.containing(x + 0.5, y + 1.0, z + 0.5),
                        MobSpawnType.MOB_SUMMONED
                );
                if (spawned != null) {
                    spawned.setYRot(serverLevel.getRandom().nextFloat() * 360.0f);
                }
            }
            serverLevel.sendParticles(BornInChaosV1ModParticleTypes.FLESHSPLASH.get(), x + 0.5, y + 1.0, z + 0.5, 3, 0.2, 0.2, 0.2, 0.1);
        }
    }
}
