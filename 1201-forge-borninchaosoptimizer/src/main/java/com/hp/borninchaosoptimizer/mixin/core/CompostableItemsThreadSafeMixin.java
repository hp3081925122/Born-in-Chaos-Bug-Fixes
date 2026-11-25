package com.hp.borninchaosoptimizer.mixin.core;

import com.hp.borninchaosoptimizer.config.BicOptimizerConfig;
import net.mcreator.borninchaosv.init.BornInChaosV1ModCompostableItems;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 修复 BornInChaosV1ModCompostableItems 在并发的 FMLCommonSetupEvent 中未使用 enqueueWork，
 * 直接向非线程安全的 ComposterBlock.COMPOSTABLES 写入导致多 MCreator 模组同装时开服随机崩溃的竞态缺陷。
 */
@Mixin(value = BornInChaosV1ModCompostableItems.class, remap = false)
public abstract class CompostableItemsThreadSafeMixin {
    @Unique
    private static final ThreadLocal<Boolean> BIC$IN_ENQUEUE_WORK = new ThreadLocal<>();

    @Inject(method = "addComposterItems", at = @At("HEAD"), cancellable = true)
    private static void bic$deferComposterRegistration(FMLCommonSetupEvent event, CallbackInfo ci) {
        if (event == null
                || Boolean.TRUE.equals(BIC$IN_ENQUEUE_WORK.get())
                || !BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.enableGlobalFixes)) {
            return;
        }
        ci.cancel();
        event.enqueueWork(() -> {
            BIC$IN_ENQUEUE_WORK.set(Boolean.TRUE);
            try {
                BornInChaosV1ModCompostableItems.addComposterItems(event);
            } finally {
                BIC$IN_ENQUEUE_WORK.remove();
            }
        });
    }
}
