package com.hp.borninchaosoptimizer.mixin.core;

import com.hp.borninchaosoptimizer.Borninchaosoptimizer;
import com.hp.borninchaosoptimizer.config.BicOptimizerConfig;
import com.hp.borninchaosoptimizer.util.WorkQueueHolder;
import net.mcreator.borninchaosv.BornInChaosV1Mod;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.fml.util.thread.SidedThreadGroups;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * 修复原模组延时任务队列的四大问题：
 * 1. ConcurrentLinkedQueue.removeAll(ArrayList) 导致的 O(N*M) 二次方删除开销；
 * 2. tick <= 0 时递减为负数永不到期导致的永久内存与世界泄漏；
 * 3. 单个到期任务异常导致同 tick 后续任务中断且无法出队；
 * 4. 绑定静态队列供关服时清理，防止单人切档跨世界污染。
 */
@Mixin(value = BornInChaosV1Mod.class, remap = false)
public abstract class BornInChaosV1ModMixin {
    @Shadow(remap = false)
    @Final
    private static Collection<AbstractMap.SimpleEntry<Runnable, Integer>> workQueue;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void bic$bindWorkQueue(CallbackInfo ci) {
        WorkQueueHolder.bind(workQueue);
    }

    @Inject(method = "queueServerWork", at = @At("HEAD"), cancellable = true)
    private static void bic$guardQueueServerWork(int tick, Runnable action, CallbackInfo ci) {
        if (!BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.enableGlobalFixes)
                || !BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.optimizeWorkQueue)) {
            return;
        }
        ci.cancel();
        if (action == null) {
            return;
        }
        if (Thread.currentThread().getThreadGroup() == SidedThreadGroups.SERVER) {
            workQueue.add(new AbstractMap.SimpleEntry<>(action, Math.max(1, tick)));
        }
    }

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void bic$optimizedServerTick(TickEvent.ServerTickEvent event, CallbackInfo ci) {
        if (!BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.enableGlobalFixes)
                || !BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.optimizeWorkQueue)) {
            return;
        }
        ci.cancel();
        if (event.phase != TickEvent.Phase.END || workQueue.isEmpty()) {
            return;
        }
        List<Runnable> dueActions = null;
        Iterator<AbstractMap.SimpleEntry<Runnable, Integer>> it = workQueue.iterator();
        while (it.hasNext()) {
            AbstractMap.SimpleEntry<Runnable, Integer> entry = it.next();
            if (entry == null) {
                it.remove();
                continue;
            }
            Integer remainingObj = entry.getValue();
            int remaining = remainingObj == null ? 0 : remainingObj - 1;
            if (remaining <= 0) {
                it.remove();
                Runnable action = entry.getKey();
                if (action != null) {
                    if (dueActions == null) {
                        dueActions = new ArrayList<>();
                    }
                    dueActions.add(action);
                }
            } else {
                entry.setValue(remaining);
            }
        }
        if (dueActions != null) {
            for (Runnable action : dueActions) {
                try {
                    action.run();
                } catch (Throwable t) {
                    Borninchaosoptimizer.LOGGER.error("[BornInChaosOptimizer] Exception while executing queued Born in Chaos server work.", t);
                }
            }
        }
    }
}
