package com.hp.borninchaosoptimizer.mixin;

import java.util.concurrent.atomic.AtomicBoolean;
import net.mcreator.borninchaosv.init.BornInChaosV1ModGameRules;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 修复 1.21.1 原模组 BornInChaosV1ModGameRules 在并发的 FMLCommonSetupEvent 中未使用 enqueueWork，
 * 直接连续调用 42 次 GameRules.register() 写入非线程安全 TreeMap（GameRules.GAME_RULE_TYPES）的开服竞态隐患，
 * 同时通过幂等原子锁防止异常重复触发导致 Duplicate game rule registration 崩溃。
 */
@Mixin(value = BornInChaosV1ModGameRules.class, remap = false)
public abstract class GameRulesThreadSafeMixin {
    @Unique
    private static final ThreadLocal<Boolean> BIC_IN_ENQUEUE_WORK = new ThreadLocal<>();
    @Unique
    private static final AtomicBoolean BIC_REGISTERED = new AtomicBoolean(false);

    @Inject(method = "registerGameRules", at = @At("HEAD"), cancellable = true)
    private static void bic$deferGameRuleRegistration(FMLCommonSetupEvent event, CallbackInfo ci) {
        if (Boolean.TRUE.equals(BIC_IN_ENQUEUE_WORK.get())) {
            return;
        }
        if (event == null) {
            return;
        }
        ci.cancel();
        if (!BIC_REGISTERED.compareAndSet(false, true)) {
            return;
        }
        event.enqueueWork(() -> {
            BIC_IN_ENQUEUE_WORK.set(Boolean.TRUE);
            try {
                BornInChaosV1ModGameRules.registerGameRules(event);
            } finally {
                BIC_IN_ENQUEUE_WORK.remove();
            }
        });
    }
}
