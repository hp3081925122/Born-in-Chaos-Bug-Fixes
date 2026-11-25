package com.hp.borninchaosoptimizer.mixin.entity;

import com.hp.borninchaosoptimizer.config.BicOptimizerConfig;
import com.hp.borninchaosoptimizer.util.BicSummonCapHelper;
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
 * 修正母蜘蛛受击召唤小蜘蛛过程：
 * 1. 限定服务端执行；
 * 2. 修正第二只小蜘蛛在 x + 2.0 处生成时 WEB_SPLASH 粒子误发到 x - 2.0 的坐标笔误；
 * 3. 单次读取目标格 BlockState，消除重复查询。
 */
@Mixin(value = MotherSpiderSummonProcedure.class, remap = false)
public abstract class MotherSpiderSummonProcedureMixin {
    @Inject(method = "execute", at = @At("HEAD"), cancellable = true)
    private static void bic$fixSummonParticlesAndSide(LevelAccessor world, double y, Entity entity, CallbackInfo ci) {
        if (!BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.enableGlobalFixes)
                || !BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.fixEntityHurtOrder)) {
            return;
        }
        ci.cancel();
        if (entity == null || world == null || world.isClientSide() || !(world instanceof ServerLevel serverLevel)) {
            return;
        }
        if (!(entity instanceof LivingEntity living)) {
            return;
        }
        if (living.hasEffect(BornInChaosV1ModMobEffects.BLOCK_BREAK.get()) || entity.isInLava()) {
            return;
        }
        if (living.getHealth() >= living.getMaxHealth()) {
            return;
        }
        living.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 80, 0, false, false));
        living.addEffect(new MobEffectInstance(BornInChaosV1ModMobEffects.BLOCK_BREAK.get(), 100, 0, false, false));

        bic$trySpawnBabySpider(serverLevel, entity, entity.getX() - 2.0, y + 1.0, entity.getZ() + 0.5);
        bic$trySpawnBabySpider(serverLevel, entity, entity.getX() + 2.0, y + 1.0, entity.getZ() + 0.5);
    }

    @Unique
    private static void bic$trySpawnBabySpider(ServerLevel level, Entity mother, double sx, double sy, double sz) {
        BlockPos pos = BlockPos.containing(sx, sy, sz);
        BlockState state = level.getBlockState(pos);
        if (!state.canOcclude() || state.getBlock() == Blocks.SNOW) {
            Entity spawned = BicSummonCapHelper.spawnWithCap(BornInChaosV1ModEntities.BABY_SPIDER.get(), level, pos, MobSpawnType.MOB_SUMMONED);
            if (spawned != null) {
                spawned.setYRot(mother.getYRot());
                spawned.setYBodyRot(mother.getYRot());
                spawned.setYHeadRot(mother.getYRot());
                spawned.setXRot(mother.getXRot());
            }
            level.sendParticles(ParticleTypes.POOF, sx, sy, sz, 3, 0.3, 0.3, 0.3, 0.1);
            level.sendParticles(BornInChaosV1ModParticleTypes.WEB_SPLASH.get(), sx, sy, sz, 6, 0.3, 0.3, 0.3, 0.1);
        }
    }
}
