package com.hp.borninchaosoptimizer.mixin;

import com.hp.borninchaosoptimizer.config.OptimizerConfig;
import com.hp.borninchaosoptimizer.util.Batch5ProcedureHelper;
import net.mcreator.borninchaosv.init.BornInChaosV1ModParticleTypes;
import net.mcreator.borninchaosv.procedures.PumpkinheadTelProcedure;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.LevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 修复南瓜头受击瞬移过程的坐标符号错误（检查 x + 0.5 却传送到 x - 0.5）以及 z - 8.0 兜底分支无墙体遮挡检查导致瞬移卡墙的缺陷。
 */
@Mixin(value = PumpkinheadTelProcedure.class, remap = false)
public abstract class PumpkinheadTelFixMixin {
    @Inject(method = "execute", at = @At("HEAD"), cancellable = true)
    private static void bic$fixPumpkinheadTeleport(LevelAccessor world, double x, double y, double z, Entity entity, CallbackInfo ci) {
        if (!OptimizerConfig.isEnabled(OptimizerConfig.FIX_BOSS_TELEPORT_INTO_WALLS)) {
            return;
        }
        ci.cancel();
        if (!(entity instanceof LivingEntity living) || living.getHealth() < 30.0f || Math.random() >= 0.9) {
            return;
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
        boolean teleported = false;
        for (int i = 0; i < 3; i++) {
            if (Math.random() < 0.3 && Batch5ProcedureHelper.isSafeTeleportSpot(world, candidates[i][0], candidates[i][1], candidates[i][2], false)) {
                Batch5ProcedureHelper.teleportEntityAndSyncPlayer(entity, candidates[i][0], candidates[i][1], candidates[i][2]);
                teleported = true;
                break;
            }
        }
        if (!teleported) {
            for (int i = 3; i >= 0; i--) {
                if (Batch5ProcedureHelper.isSafeTeleportSpot(world, candidates[i][0], candidates[i][1], candidates[i][2], false)) {
                    Batch5ProcedureHelper.teleportEntityAndSyncPlayer(entity, candidates[i][0], candidates[i][1], candidates[i][2]);
                    break;
                }
            }
        }
        if (world instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(BornInChaosV1ModParticleTypes.SOUL_FIRE.get(), x, y, z, 9, 1.0, 1.0, 1.0, 1.0);
        }
        if (!world.isClientSide()) {
            Batch5ProcedureHelper.playSoundBothSides(world, x, y, z, "entity.enderman.teleport", 0.8f, 1.0f);
        }
    }
}
