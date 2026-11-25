package com.hp.borninchaosoptimizer.mixin.procedure;

import com.hp.borninchaosoptimizer.config.BicOptimizerConfig;
import net.mcreator.borninchaosv.entity.PumpkinBombEntity;
import net.mcreator.borninchaosv.init.BornInChaosV1ModBlocks;
import net.mcreator.borninchaosv.init.BornInChaosV1ModMobEffects;
import net.mcreator.borninchaosv.init.BornInChaosV1ModParticleTypes;
import net.mcreator.borninchaosv.procedures.PumpkinBombPriObnovlieniiTikaSushchnostiProcedure;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Map;

/**
 * 修复南瓜炸弹每 tick 引爆过程：
 * 1. 仅服务端执行，消除客户端放置幽灵邪土方块；
 * 2. 增加实体移除锁防止重复爆炸，并将全实体流式排序替换为精确 LivingEntity 扫描。
 */
@Mixin(value = PumpkinBombPriObnovlieniiTikaSushchnostiProcedure.class, remap = false)
public abstract class PumpkinBombTickProcedureMixin {
    @Inject(method = "execute", at = @At("HEAD"), cancellable = true)
    private static void bicOpt$safeTick(LevelAccessor world, double x, double y, double z, Entity entity, CallbackInfo ci) {
        if (!BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.enableGlobalFixes)
                || !BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.fixEntityTickClientSideMutations)) {
            return;
        }
        ci.cancel();
        if (!(entity instanceof LivingEntity living) || world.isClientSide() || entity.isRemoved()
                || !(world instanceof ServerLevel serverLevel)) {
            return;
        }

        MobEffect blockBreak = BornInChaosV1ModMobEffects.BLOCK_BREAK.get();
        if (!living.hasEffect(MobEffects.DAMAGE_RESISTANCE) || !living.hasEffect(blockBreak)) {
            return;
        }

        serverLevel.sendParticles(BornInChaosV1ModParticleTypes.DARK_SMOKE.get(), x, y + 1.0, z, 1, 0.2, 0.2, 0.2, 0.1);
        if (entity instanceof PumpkinBombEntity bomb) {
            bomb.setAnimation("boom");
        }

        MobEffectInstance breakInst = living.getEffect(blockBreak);
        if (breakInst != null && breakInst.getDuration() <= 7) {
            entity.discard();
            serverLevel.explode(null, x, y, z, 3.0f, Level.ExplosionInteraction.NONE);
            serverLevel.sendParticles(BornInChaosV1ModParticleTypes.PUMPKIN_EXPLOSION.get(), x, y + 1.0, z, 3, 0.6, 0.6, 0.6, 0.1);
            serverLevel.sendParticles(BornInChaosV1ModParticleTypes.INFERNAL_SURGE.get(), x, y, z, 16, 2.0, 0.3, 2.0, 0.1);

            AABB area = new AABB(x - 3.5, y - 3.5, z - 3.5, x + 3.5, y + 3.5, z + 3.5);
            List<LivingEntity> victims = serverLevel.getEntitiesOfClass(LivingEntity.class, area, v -> v != entity && !v.isBlocking());
            MobEffect infernalFlame = BornInChaosV1ModMobEffects.INFERNAL_FLAME.get();
            for (LivingEntity victim : victims) {
                victim.addEffect(new MobEffectInstance(infernalFlame, 240, 0));
            }

            BlockPos belowPos = BlockPos.containing(x, y - 1.0, z);
            BlockState oldState = serverLevel.getBlockState(belowPos);
            if (oldState.canOcclude() && !oldState.is(Blocks.BEDROCK)) {
                BlockState newState = BornInChaosV1ModBlocks.FEL_SOIL.get().defaultBlockState();
                for (Map.Entry<Property<?>, Comparable<?>> entry : oldState.getValues().entrySet()) {
                    Property<?> targetProp = newState.getBlock().getStateDefinition().getProperty(entry.getKey().getName());
                    if (targetProp != null) {
                        newState = bicOpt$copyPropertyUnchecked(newState, targetProp, entry.getValue());
                    }
                }
                serverLevel.setBlock(belowPos, newState, 3);
            }
        }
    }

    @Unique
    @SuppressWarnings({"unchecked", "rawtypes"})
    private static BlockState bicOpt$copyPropertyUnchecked(BlockState state, Property prop, Comparable val) {
        try {
            if (state.hasProperty(prop)) {
                return (BlockState) state.setValue(prop, val);
            }
        } catch (Exception ignored) {
        }
        return state;
    }
}
