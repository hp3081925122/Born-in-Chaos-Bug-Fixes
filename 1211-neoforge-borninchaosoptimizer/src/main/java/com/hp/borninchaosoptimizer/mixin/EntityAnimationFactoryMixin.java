package com.hp.borninchaosoptimizer.mixin;

import com.hp.borninchaosoptimizer.config.OptimizerConfig;
import com.hp.borninchaosoptimizer.util.IBicSyncedAnimatable;
import net.mcreator.borninchaosv.init.EntityAnimationFactory;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 将 EntityAnimationFactory 对全服所有实体每刻 83 次顺序 instanceof 检查优化为单次 O(1) 接口分发，
 * 并修复服务端在 EntityTickEvent.Pre 同刻立即将动画重置回 undefined 导致客户端收不到格挡/技能动画包的缺陷。
 */
@Mixin(value = EntityAnimationFactory.class, remap = false)
public abstract class EntityAnimationFactoryMixin {
    @Inject(method = "onEntityTick", at = @At("HEAD"), cancellable = true)
    private static void bic$fastEntityAnimationTick(EntityTickEvent.Pre event, CallbackInfo ci) {
        if (!OptimizerConfig.isEnabled(OptimizerConfig.OPTIMIZE_ENTITY_ANIMATION_SYNC)) {
            return;
        }
        ci.cancel();
        if (event == null) {
            return;
        }
        Entity entity = event.getEntity();
        if (entity instanceof IBicSyncedAnimatable syncable) {
            syncable.bic$handleAnimationSyncTick(entity.level().isClientSide());
        }
    }
}
