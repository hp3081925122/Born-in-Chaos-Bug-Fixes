package com.hp.borninchaosoptimizer.mixin;

import com.hp.borninchaosoptimizer.config.OptimizerConfig;
import java.util.List;
import net.mcreator.borninchaosv.entity.PumpkinBombEntity;
import net.mcreator.borninchaosv.init.BornInChaosV1ModBlocks;
import net.mcreator.borninchaosv.init.BornInChaosV1ModMobEffects;
import net.mcreator.borninchaosv.init.BornInChaosV1ModParticleTypes;
import net.mcreator.borninchaosv.procedures.PumpkinBombPriObnovlieniiTikaSushchnostiProcedure;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
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

/**
 * 修复南瓜炸弹实体每刻逻辑：
 * 1. 仅服务端权威执行，消除客户端在爆炸前夕放置幽灵邪土方块与已销毁实体重复爆炸；
 * 2. 将全实体流式排序扫描收敛为 LivingEntity.class 精确查询。
 */
@Mixin(value = PumpkinBombPriObnovlieniiTikaSushchnostiProcedure.class, remap = false)
public abstract class PumpkinBombTickOptimizeMixin {
    @Inject(method = "execute", at = @At("HEAD"), cancellable = true)
    private static void bic$optimizePumpkinBombTick(LevelAccessor world, double x, double y, double z, Entity entity, CallbackInfo ci) {
        if (!OptimizerConfig.isEnabled(OptimizerConfig.FIX_CLIENT_SIDE_GHOST_MUTATIONS)) {
            return;
        }
        ci.cancel();
        if (!(entity instanceof LivingEntity living) || world.isClientSide() || entity.isRemoved() || !(world instanceof ServerLevel serverLevel)) {
            return;
        }
        if (!living.hasEffect(MobEffects.DAMAGE_RESISTANCE)) {
            return;
        }
        MobEffectInstance blockBreak = living.getEffect(BornInChaosV1ModMobEffects.BLOCK_BREAK);
        if (blockBreak == null) {
            return;
        }
        serverLevel.sendParticles(BornInChaosV1ModParticleTypes.DARK_SMOKE.get(), x, y + 1.0, z, 1, 0.2, 0.2, 0.2, 0.1);
        if (entity instanceof PumpkinBombEntity bomb) {
            bomb.setAnimation("boom");
        }
        if (blockBreak.getDuration() <= 7) {
            entity.discard();
            serverLevel.explode(null, x, y, z, 3.0f, Level.ExplosionInteraction.NONE);
            serverLevel.sendParticles(BornInChaosV1ModParticleTypes.PUMPKIN_EXPLOSION.get(), x, y + 1.0, z, 3, 0.6, 0.6, 0.6, 0.1);
            serverLevel.sendParticles(BornInChaosV1ModParticleTypes.INFERNAL_SURGE.get(), x, y, z, 16, 2.0, 0.3, 2.0, 0.1);

            AABB area = new AABB(x - 3.5, y - 3.5, z - 3.5, x + 3.5, y + 3.5, z + 3.5);
            List<LivingEntity> victims = serverLevel.getEntitiesOfClass(LivingEntity.class, area);
            for (int i = 0, size = victims.size(); i < size; i++) {
                LivingEntity victim = victims.get(i);
                if (!victim.isBlocking()) {
                    victim.addEffect(new MobEffectInstance(BornInChaosV1ModMobEffects.INFERNAL_FLAME, 240, 0));
                }
            }

            BlockPos belowPos = BlockPos.containing(x, y - 1.0, z);
            BlockState oldState = serverLevel.getBlockState(belowPos);
            if (oldState.canOcclude() && oldState.getBlock() != Blocks.BEDROCK) {
                BlockState newState = BornInChaosV1ModBlocks.FEL_SOIL.get().defaultBlockState();
                for (Property<?> oldProp : oldState.getProperties()) {
                    newState = bic$copyProperty(oldState, newState, oldProp);
                }
                serverLevel.setBlock(belowPos, newState, 3);
            }
        }
    }

    @Unique
    @SuppressWarnings({"unchecked", "rawtypes"})
    private static BlockState bic$copyProperty(BlockState oldState, BlockState newState, Property oldProp) {
        Property newProp = newState.getBlock().getStateDefinition().getProperty(oldProp.getName());
        if (newProp != null && newState.hasProperty(newProp)) {
            try {
                return (BlockState) newState.setValue(newProp, oldState.getValue(oldProp));
            } catch (Exception ignored) {
            }
        }
        return newState;
    }
}
