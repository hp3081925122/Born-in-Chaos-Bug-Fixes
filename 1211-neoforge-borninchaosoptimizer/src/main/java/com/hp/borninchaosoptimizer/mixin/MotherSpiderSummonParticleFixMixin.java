package com.hp.borninchaosoptimizer.mixin;

import com.hp.borninchaosoptimizer.config.OptimizerConfig;
import net.mcreator.borninchaosv.init.BornInChaosV1ModEntities;
import net.mcreator.borninchaosv.init.BornInChaosV1ModMobEffects;
import net.mcreator.borninchaosv.init.BornInChaosV1ModParticleTypes;
import net.mcreator.borninchaosv.procedures.MotherSpiderSummonProcedure;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 修正母蜘蛛受击召唤第二只幼蛛（x + 2.0）时误将蛛网飞溅粒子发送到左侧（x - 2.0）的坐标笔误。
 */
@Mixin(value = MotherSpiderSummonProcedure.class, remap = false)
public abstract class MotherSpiderSummonParticleFixMixin {
    @Inject(method = "execute", at = @At("HEAD"), cancellable = true)
    private static void bic$fixSecondSpiderWebParticle(LevelAccessor world, double y, Entity entity, CallbackInfo ci) {
        if (!OptimizerConfig.isEnabled(OptimizerConfig.FIX_ENTITY_HURT_ORDER_AND_SUMMON_PARTICLE)) {
            return;
        }
        ci.cancel();
        if (!(entity instanceof LivingEntity living) || living.hasEffect(BornInChaosV1ModMobEffects.BLOCK_BREAK) || living.isInLava()) {
            return;
        }
        if (living.getHealth() >= living.getMaxHealth() || !(world instanceof ServerLevel serverLevel)) {
            return;
        }
        living.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 80, 0, false, false));
        living.addEffect(new MobEffectInstance(BornInChaosV1ModMobEffects.BLOCK_BREAK, 100, 0, false, false));
        bic$spawnBabySpiderAt(serverLevel, entity.getX() - 2.0, y + 1.0, entity.getZ() + 0.5, entity);
        bic$spawnBabySpiderAt(serverLevel, entity.getX() + 2.0, y + 1.0, entity.getZ() + 0.5, entity);
    }

    @Unique
    private static void bic$spawnBabySpiderAt(ServerLevel serverLevel, double sx, double sy, double sz, Entity mother) {
        BlockPos pos = BlockPos.containing(sx, sy, sz);
        BlockState state = serverLevel.getBlockState(pos);
        if (!state.canOcclude() || state.is(Blocks.SNOW)) {
            Entity baby = BornInChaosV1ModEntities.BABY_SPIDER.get().spawn(serverLevel, pos, MobSpawnType.MOB_SUMMONED);
            if (baby != null) {
                baby.setYRot(mother.getYRot());
                baby.setYBodyRot(mother.getYRot());
                baby.setYHeadRot(mother.getYRot());
                baby.setXRot(mother.getXRot());
            }
            serverLevel.sendParticles(ParticleTypes.POOF, sx, sy, sz, 3, 0.3, 0.3, 0.3, 0.1);
            serverLevel.sendParticles(BornInChaosV1ModParticleTypes.WEB_SPLASH.get(), sx, sy, sz, 6, 0.3, 0.3, 0.3, 0.1);
        }
    }
}
