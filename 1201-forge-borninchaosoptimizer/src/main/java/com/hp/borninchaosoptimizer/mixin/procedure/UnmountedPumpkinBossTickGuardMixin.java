package com.hp.borninchaosoptimizer.mixin.procedure;

import com.hp.borninchaosoptimizer.config.BicOptimizerConfig;
import net.mcreator.borninchaosv.procedures.LordPumpkinheadTicProcedure;
import net.mcreator.borninchaosv.procedures.WithoutHorsePProcedure;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.LevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 为步战状态的南瓜领主与南瓜爵士每 tick 过程增加服务端守卫与断头分裂幂等锁：
 * 阻止客户端放置幽灵邪土方块和客户端独立随机瞬移，并杜绝残血同 tick 重复刷出无头骑士与南瓜头。
 */
@Mixin(value = {
        LordPumpkinheadTicProcedure.class,
        WithoutHorsePProcedure.class
}, remap = false)
public abstract class UnmountedPumpkinBossTickGuardMixin {
    @Unique
    private static final String BIC_OPT$TRANSFORM_LOCK = "bic_opt_phase_transformed";

    @Inject(method = "execute", at = @At("HEAD"), cancellable = true)
    private static void bicOpt$guardServerAndSingleSplit(LevelAccessor world, double x, double y, double z, Entity entity, CallbackInfo ci) {
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
        if (hp < 20.0f) {
            entity.getPersistentData().putBoolean(BIC_OPT$TRANSFORM_LOCK, true);
        }
    }
}
