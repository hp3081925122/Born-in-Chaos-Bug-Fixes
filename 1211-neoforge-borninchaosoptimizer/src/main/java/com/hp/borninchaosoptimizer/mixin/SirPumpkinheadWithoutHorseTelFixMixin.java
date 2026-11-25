package com.hp.borninchaosoptimizer.mixin;

import com.hp.borninchaosoptimizer.config.OptimizerConfig;
import com.hp.borninchaosoptimizer.util.Batch5ProcedureHelper;
import net.mcreator.borninchaosv.init.BornInChaosV1ModEntities;
import net.mcreator.borninchaosv.init.BornInChaosV1ModMobEffects;
import net.mcreator.borninchaosv.init.BornInChaosV1ModParticleTypes;
import net.mcreator.borninchaosv.procedures.SerPumpkinheadWTelProcedure;
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
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 修复步行态南瓜爵士受击瞬移过程的逻辑恒真与卡墙缺陷：
 * 1. 原代码将 (block != LAVA && block != WATER) 误写为逻辑或（永远为真），导致会直接瞬移进岩浆或深水；
 * 2. 补全双格高度与 z - 8.0 兜底分支的安全遮挡判定，防止瞬移进实心墙体窒息。
 */
@Mixin(value = SerPumpkinheadWTelProcedure.class, remap = false)
public abstract class SirPumpkinheadWithoutHorseTelFixMixin {
    @Inject(method = "execute", at = @At("HEAD"), cancellable = true)
    private static void bic$fixWalkSirPumpkinheadTeleport(LevelAccessor world, double x, double y, double z, Entity entity, CallbackInfo ci) {
        if (!OptimizerConfig.isEnabled(OptimizerConfig.FIX_BOSS_TELEPORT_INTO_WALLS)) {
            return;
        }
        ci.cancel();
        if (!(entity instanceof LivingEntity living)) {
            return;
        }
        if (living.hasEffect(BornInChaosV1ModMobEffects.MAGIC_DEPLETION) || living.isOnFire()) {
            return;
        }
        if (living.getHealth() >= living.getMaxHealth() - 5.0f) {
            return;
        }
        if (!living.level().isClientSide()) {
            living.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 160, 1, false, false));
            living.addEffect(new MobEffectInstance(BornInChaosV1ModMobEffects.MAGIC_DEPLETION, 200, 0, false, false));
        }
        BlockPos currentPos = BlockPos.containing(x, y, z);
        BlockState currentState = world.getBlockState(currentPos);
        if (!currentState.canOcclude() || currentState.is(Blocks.SNOW)) {
            if (world instanceof ServerLevel serverLevel) {
                Entity spawned = BornInChaosV1ModEntities.MR_PUMPKIN.get().spawn(serverLevel, currentPos, MobSpawnType.MOB_SUMMONED);
                if (spawned != null) {
                    spawned.setYRot(entity.getYRot());
                    spawned.setYBodyRot(entity.getYRot());
                    spawned.setYHeadRot(entity.getYRot());
                    spawned.setXRot(entity.getXRot());
                }
                serverLevel.sendParticles(BornInChaosV1ModParticleTypes.SOUL_FIRE.get(), x, y, z, 9, 0.3, 0.3, 0.3, 0.1);
                serverLevel.sendParticles(ParticleTypes.POOF, x, y, z, 6, 0.3, 0.3, 0.3, 0.1);
            }
            if (!world.isClientSide()) {
                Batch5ProcedureHelper.playSoundBothSides(world, x, y, z, "entity.glow_squid.squirt", 1.0f, 1.0f);
            }
            double[][] candidates = {
                    {x + 8.0, y + 1.0, z + 0.5},
                    {x - 8.0, y + 1.0, z + 0.5},
                    {x + 0.5, y + 1.0, z + 8.0},
                    {x + 0.5, y + 1.0, z - 8.0}
            };
            for (double[] candidate : candidates) {
                if (Batch5ProcedureHelper.isSafeTeleportSpot(world, candidate[0], candidate[1], candidate[2], true)) {
                    Batch5ProcedureHelper.teleportEntityAndSyncPlayer(entity, candidate[0], candidate[1], candidate[2]);
                    break;
                }
            }
            Batch5ProcedureHelper.spawnPoofAndTeleportSound(world, x, y, z);
        }
    }
}
