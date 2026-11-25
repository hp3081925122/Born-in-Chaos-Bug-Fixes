package com.hp.borninchaosoptimizer.mixin.procedure;

import com.hp.borninchaosoptimizer.config.BicOptimizerConfig;
import net.mcreator.borninchaosv.entity.KrampusEntity;
import net.mcreator.borninchaosv.init.BornInChaosV1ModBlocks;
import net.mcreator.borninchaosv.init.BornInChaosV1ModMobEffects;
import net.mcreator.borninchaosv.procedures.KrampusPriObnovlieniiTikaSushchnostiProcedure;
import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 修复克朗普斯每 tick 过程：
 * 1. 仅服务端权威执行，消除客户端放置幽灵暗冰与幽灵积雪；
 * 2. 先判 hasEffect 再调用 removeEffect(BONE_CHILLING)，停止无条件刷爆 MobEffectEvent.Remove 总线。
 */
@Mixin(value = KrampusPriObnovlieniiTikaSushchnostiProcedure.class, remap = false)
public abstract class KrampusTickProcedureMixin {
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

        MobEffect boneChilling = BornInChaosV1ModMobEffects.BONE_CHILLING.get();
        if (living.hasEffect(boneChilling)) {
            living.removeEffect(boneChilling);
        }

        MobEffect blockBreak = BornInChaosV1ModMobEffects.BLOCK_BREAK.get();
        if (living.hasEffect(MobEffects.DAMAGE_RESISTANCE) && living.hasEffect(blockBreak)) {
            double cur = entity.getPersistentData().getDouble("appearance");
            double next = cur == 0.0 ? 1.0 : cur - 1.0;
            entity.getPersistentData().putDouble("appearance", next);
            if (next == 0.0) {
                if (entity instanceof KrampusEntity krampus) {
                    krampus.setAnimation("appearance");
                }
                living.removeEffect(MobEffects.DAMAGE_RESISTANCE);
                living.removeEffect(blockBreak);
            }
        }

        BlockPos belowPos = BlockPos.containing(x, y - 1.0, z);
        BlockState belowState = world.getBlockState(belowPos);
        Block belowBlock = belowState.getBlock();
        if (belowBlock == Blocks.WATER || belowBlock == Blocks.KELP_PLANT
                || belowBlock == Blocks.SEAGRASS || belowBlock == Blocks.TALL_SEAGRASS) {
            world.setBlock(belowPos, BornInChaosV1ModBlocks.DARK_ICE.get().defaultBlockState(), 3);
            belowState = world.getBlockState(belowPos);
        }

        BlockPos feetPos = BlockPos.containing(x, y, z);
        if (belowState.canOcclude()
                && world.getBlockState(feetPos).getBlock() == Blocks.AIR
                && living.hasEffect(BornInChaosV1ModMobEffects.SNOW_STORM.get())) {
            world.setBlock(feetPos, Blocks.SNOW.defaultBlockState(), 3);
        }
    }
}
