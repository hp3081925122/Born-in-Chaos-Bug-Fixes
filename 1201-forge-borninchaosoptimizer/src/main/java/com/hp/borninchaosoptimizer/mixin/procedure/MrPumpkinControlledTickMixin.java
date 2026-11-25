package com.hp.borninchaosoptimizer.mixin.procedure;

import com.hp.borninchaosoptimizer.config.BicOptimizerConfig;
import net.mcreator.borninchaosv.init.BornInChaosV1ModEntities;
import net.mcreator.borninchaosv.procedures.MrPumpkinPProcedure;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.LevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 修复受控南瓜先生每 tick 过程：到期消失后立即返回，避免 isBaby() 分支原地重生，并限制为服务端执行。
 */
@Mixin(value = MrPumpkinPProcedure.class, remap = false)
public abstract class MrPumpkinControlledTickMixin {
    @Unique
    private static final String BIC_OPT$REPLACE_LOCK = "bic_opt_mr_pumpkin_replaced";

    @Inject(method = "execute", at = @At("HEAD"), cancellable = true)
    private static void bicOpt$safeTick(LevelAccessor world, double x, double y, double z, Entity entity, CallbackInfo ci) {
        if (!BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.enableGlobalFixes)
                || !BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.fixBossTransformAndTeleportBugs)) {
            return;
        }
        ci.cancel();
        if (entity == null || world.isClientSide() || entity.isRemoved()) {
            return;
        }
        if (!(entity instanceof LivingEntity living) || !living.hasEffect(MobEffects.DAMAGE_RESISTANCE)) {
            entity.discard();
            if (world instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.POOF, x, y, z, 5, 0.5, 0.5, 0.5, 0.1);
            }
            return;
        }
        if (living.isBaby() && !entity.getPersistentData().getBoolean(BIC_OPT$REPLACE_LOCK)) {
            entity.getPersistentData().putBoolean(BIC_OPT$REPLACE_LOCK, true);
            if (world instanceof ServerLevel serverLevel) {
                Entity spawned = BornInChaosV1ModEntities.MR_PUMPKIN_CONTROLLED.get()
                        .spawn(serverLevel, BlockPos.containing(x, y, z), MobSpawnType.MOB_SUMMONED);
                if (spawned != null) {
                    spawned.setYRot(entity.getYRot());
                    spawned.setYBodyRot(entity.getYRot());
                    spawned.setYHeadRot(entity.getYRot());
                    spawned.setXRot(entity.getXRot());
                }
            }
            entity.discard();
            return;
        }
        if (entity.getPersistentData().getBoolean("attack_target")) {
            double current = entity.getPersistentData().getDouble("target");
            double next = current == 0.0 ? 10.0 : current - 1.0;
            entity.getPersistentData().putDouble("target", next);
            if (next == 0.0) {
                entity.getPersistentData().putBoolean("attack_target", false);
            }
        }
    }
}
