package com.hp.borninchaosoptimizer.mixin.gecko;

import com.hp.borninchaosoptimizer.api.BicAnimatableEntity;
import com.hp.borninchaosoptimizer.config.BicOptimizerConfig;
import net.mcreator.borninchaosv.init.EntityAnimationFactory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 将 EntityAnimationFactory.onEntityTick 从对全服所有生物执行 83 次独立 instanceof 检查，
 * 优化为单次 BicAnimatableEntity 接口判断与 O(1) 虚方法调用。
 */
@Mixin(value = EntityAnimationFactory.class, remap = false)
public abstract class EntityAnimationFactoryMixin {
    @Inject(method = "onEntityTick", at = @At("HEAD"), cancellable = true)
    private static void bic$fastEntityAnimationTick(LivingEvent.LivingTickEvent event, CallbackInfo ci) {
        if (!BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.enableGlobalFixes)
                || !BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.optimizeAnimationFactories)) {
            return;
        }
        ci.cancel();
        if (event == null) {
            return;
        }
        LivingEntity entity = event.getEntity();
        if (entity instanceof BicAnimatableEntity animatable) {
            animatable.bic$handleAnimationSyncTick(entity.level().isClientSide());
        }
    }
}
