package com.hp.borninchaosoptimizer.mixin.procedure;

import com.hp.borninchaosoptimizer.config.BicOptimizerConfig;
import net.mcreator.borninchaosv.procedures.LifestealerPriObnovlieniiTikaSushchnostiProcedure;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.LevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 为窃命者伪装形态每 tick 过程增加服务端守卫，并与攻击现真身过程共享同一把幂等锁，
 * 彻底杜绝同 tick 受击与攻击同时触发刷出两只窃命者真身首领，同时消除客户端幽灵暗冰方块。
 */
@Mixin(value = LifestealerPriObnovlieniiTikaSushchnostiProcedure.class, remap = false)
public abstract class LifestealerDisguiseTickGuardMixin {
    @Unique
    private static final String BIC_OPT$TRUE_FORM_LOCK = "bic_opt_true_form_spawned";

    @Inject(method = "execute", at = @At("HEAD"), cancellable = true)
    private static void bicOpt$guardTrueFormAndIce(LevelAccessor world, double x, double y, double z, Entity entity, CallbackInfo ci) {
        if (!BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.enableGlobalFixes)
                || !BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.fixBossTransformAndTeleportBugs)) {
            return;
        }
        if (entity == null || world.isClientSide() || entity.isRemoved()
                || entity.getPersistentData().getBoolean(BIC_OPT$TRUE_FORM_LOCK)) {
            ci.cancel();
            return;
        }
        float hp = entity instanceof LivingEntity living ? living.getHealth() : -1.0f;
        float maxHp = entity instanceof LivingEntity living ? living.getMaxHealth() : -1.0f;
        if (hp < maxHp) {
            entity.getPersistentData().putBoolean(BIC_OPT$TRUE_FORM_LOCK, true);
        }
    }
}
