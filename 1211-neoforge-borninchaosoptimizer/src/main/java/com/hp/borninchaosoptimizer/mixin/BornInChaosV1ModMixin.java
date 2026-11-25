package com.hp.borninchaosoptimizer.mixin;

import com.hp.borninchaosoptimizer.Borninchaosoptimizer;
import com.hp.borninchaosoptimizer.config.OptimizerConfig;
import com.hp.borninchaosoptimizer.util.IBicWorkQueueAccess;
import com.hp.borninchaosoptimizer.util.WorkQueueBridge;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import net.mcreator.borninchaosv.BornInChaosV1Mod;
import net.minecraft.util.Tuple;
import net.neoforged.fml.util.thread.SidedThreadGroups;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 修复原模组全局延时任务队列在 <=0 刻任务下永久驻留、异常中断导致任务残留泄漏、每刻空队列分配及 O(N*M) 移除问题。
 */
@Mixin(value = BornInChaosV1Mod.class, remap = false)
public abstract class BornInChaosV1ModMixin implements IBicWorkQueueAccess {
    @Shadow(remap = false)
    @Final
    private static Collection<Tuple<Runnable, Integer>> workQueue;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void bic$bindWorkQueueInstance(CallbackInfo ci) {
        WorkQueueBridge.bind(this);
    }

    @Override
    public void bic$clearWorkQueue() {
        int size = workQueue.size();
        workQueue.clear();
        if (size > 0) {
            Borninchaosoptimizer.LOGGER.info("[BornInChaosOptimizer] Cleared {} pending server work tasks on server stopping.", size);
        }
    }

    @Inject(method = "queueServerWork", at = @At("HEAD"), cancellable = true)
    private static void bic$guardQueueServerWork(int tick, Runnable action, CallbackInfo ci) {
        if (!OptimizerConfig.isEnabled(OptimizerConfig.FIX_WORK_QUEUE_LEAK)) {
            return;
        }
        ci.cancel();
        if (action == null) {
            return;
        }
        if (Thread.currentThread().getThreadGroup() == SidedThreadGroups.SERVER) {
            int safeTick = Math.max(1, tick);
            workQueue.add(new Tuple<>(action, safeTick));
        }
    }

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void bic$optimizedServerTick(ServerTickEvent.Post event, CallbackInfo ci) {
        if (!OptimizerConfig.isEnabled(OptimizerConfig.FIX_WORK_QUEUE_LEAK)) {
            return;
        }
        ci.cancel();
        if (workQueue.isEmpty()) {
            return;
        }
        List<Runnable> readyActions = null;
        Iterator<Tuple<Runnable, Integer>> iterator = workQueue.iterator();
        while (iterator.hasNext()) {
            Tuple<Runnable, Integer> work = iterator.next();
            if (work == null || work.getA() == null || work.getB() == null) {
                iterator.remove();
                continue;
            }
            int next = work.getB() - 1;
            if (next <= 0) {
                if (readyActions == null) {
                    readyActions = new ArrayList<>();
                }
                readyActions.add(work.getA());
                iterator.remove();
            } else {
                work.setB(next);
            }
        }
        if (readyActions != null) {
            for (Runnable runnable : readyActions) {
                try {
                    runnable.run();
                } catch (Throwable t) {
                    Borninchaosoptimizer.LOGGER.error("[BornInChaosOptimizer] Exception while executing queued Born in Chaos server task", t);
                }
            }
        }
    }
}
