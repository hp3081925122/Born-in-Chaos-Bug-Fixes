package com.hp.borninchaosoptimizer.mixin.combat;

import com.hp.borninchaosoptimizer.config.BicOptimizerConfig;
import net.mcreator.borninchaosv.entity.LifestealerEntity;
import net.mcreator.borninchaosv.procedures.LifestealerAtackProcedure;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelAccessor;
import net.minecraftforge.eventbus.api.Event;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 为窃命者（Lifestealer）攻击现出真身过程增加服务端限定与一次性幂等锁，
 * 防止同 tick 多重攻击事件刷出双倍 LIFESTEALER_TRUE_FORM Boss。
 */
@Mixin(value = LifestealerAtackProcedure.class, remap = false)
public abstract class LifestealerAtackProcedureMixin {
    @Unique
    private static final String BIC_TRANSFORMED_LOCK = "bic_opt_true_form_spawned";

    @Inject(
            method = "execute(Lnet/minecraftforge/eventbus/api/Event;Lnet/minecraft/world/level/LevelAccessor;DDDLnet/minecraft/world/entity/Entity;Lnet/minecraft/world/entity/Entity;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void bic$guardSingleTrueFormTransform(
            Event event, LevelAccessor world, double x, double y, double z, Entity entity, Entity sourceentity, CallbackInfo ci
    ) {
        if (!BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.enableGlobalFixes)
                || !BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.fixGlobalCombatEvents)) {
            return;
        }
        if (world == null || world.isClientSide() || entity == null || sourceentity == null) {
            ci.cancel();
            return;
        }
        if (sourceentity instanceof LifestealerEntity) {
            if (sourceentity.isRemoved() || sourceentity.getPersistentData().getBoolean(BIC_TRANSFORMED_LOCK)) {
                ci.cancel();
                return;
            }
            sourceentity.getPersistentData().putBoolean(BIC_TRANSFORMED_LOCK, true);
        }
    }
}
