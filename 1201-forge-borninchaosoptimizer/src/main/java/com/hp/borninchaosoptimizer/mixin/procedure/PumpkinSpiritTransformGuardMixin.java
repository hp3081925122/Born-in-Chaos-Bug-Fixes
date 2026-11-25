package com.hp.borninchaosoptimizer.mixin.procedure;

import com.hp.borninchaosoptimizer.config.BicOptimizerConfig;
import net.mcreator.borninchaosv.procedures.PumpkinSpiritPriObnovlieniiTaktaSushchnostiProcedure;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 为南瓜之灵着火转化为灼热之灵过程增加实体移除检查与一次性变身锁，防止重复生成。
 */
@Mixin(value = PumpkinSpiritPriObnovlieniiTaktaSushchnostiProcedure.class, remap = false)
public abstract class PumpkinSpiritTransformGuardMixin {
    @Unique
    private static final String BIC_OPT$TRANSFORM_LOCK = "bic_opt_phase_transformed";

    @Inject(method = "execute", at = @At("HEAD"), cancellable = true)
    private static void bicOpt$guardFireTransform(LevelAccessor world, double x, double y, double z, Entity entity, CallbackInfo ci) {
        if (!BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.enableGlobalFixes)
                || !BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.fixBossTransformAndTeleportBugs)) {
            return;
        }
        if (entity == null || world.isClientSide() || entity.isRemoved()
                || entity.getPersistentData().getBoolean(BIC_OPT$TRANSFORM_LOCK)) {
            ci.cancel();
            return;
        }
        if (entity.isOnFire()) {
            entity.getPersistentData().putBoolean(BIC_OPT$TRANSFORM_LOCK, true);
        }
    }
}
