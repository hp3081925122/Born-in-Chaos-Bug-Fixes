package com.hp.borninchaosoptimizer.mixin.procedure;

import com.hp.borninchaosoptimizer.config.BicOptimizerConfig;
import net.mcreator.borninchaosv.procedures.SupremeBonecallerPriObnovlieniiTaktaSushchnostiProcedure;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.LevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 为至尊唤骨者一阶段转二阶段过程增加服务端守卫与一次性变身锁，防止二阶段首领双倍复制。
 */
@Mixin(value = SupremeBonecallerPriObnovlieniiTaktaSushchnostiProcedure.class, remap = false)
public abstract class SupremeBonecallerTickGuardMixin {
    @Unique
    private static final String BIC_OPT$TRANSFORM_LOCK = "bic_opt_phase_transformed";

    @Inject(method = "execute", at = @At("HEAD"), cancellable = true)
    private static void bicOpt$guardSingleTransform(LevelAccessor world, double x, double y, double z, Entity entity, CallbackInfo ci) {
        if (!BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.enableGlobalFixes)
                || !BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.fixBossTransformAndTeleportBugs)) {
            return;
        }
        if (entity == null || world.isClientSide() || entity.isRemoved()
                || entity.getPersistentData().getBoolean(BIC_OPT$TRANSFORM_LOCK)) {
            ci.cancel();
            return;
        }
        float hp = entity instanceof LivingEntity living ? living.getHealth() : -1.0f;
        if (hp <= 15.0f) {
            entity.getPersistentData().putBoolean(BIC_OPT$TRANSFORM_LOCK, true);
        }
    }
}
