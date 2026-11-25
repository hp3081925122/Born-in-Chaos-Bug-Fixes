package com.hp.borninchaosoptimizer.mixin.procedure;

import com.hp.borninchaosoptimizer.config.BicOptimizerConfig;
import net.mcreator.borninchaosv.procedures.PhantomCreeperKoghdaSushchnostRanienaProcedure;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 修复魅影苦力怕受击分裂过程在散弹、荆棘或横扫同 tick 多次命中时重复分裂刷出大量复制体的 Bug。
 */
@Mixin(value = PhantomCreeperKoghdaSushchnostRanienaProcedure.class, remap = false)
public abstract class PhantomCreeperHurtSplitGuardMixin {
    @Unique
    private static final String BIC_OPT$SPLIT_LOCK = "bic_opt_creeper_split";

    @Inject(method = "execute", at = @At("HEAD"), cancellable = true)
    private static void bicOpt$guardSingleSplit(LevelAccessor world, double x, double y, double z, Entity entity, Entity sourceentity, CallbackInfo ci) {
        if (!BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.enableGlobalFixes)
                || !BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.fixBossTransformAndTeleportBugs)) {
            return;
        }
        if (entity == null || sourceentity == null || world.isClientSide()
                || entity.isRemoved() || entity.getPersistentData().getBoolean(BIC_OPT$SPLIT_LOCK)) {
            ci.cancel();
            return;
        }
        entity.getPersistentData().putBoolean(BIC_OPT$SPLIT_LOCK, true);
    }
}
