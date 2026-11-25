package com.hp.borninchaosoptimizer.util;

import java.util.AbstractMap;
import java.util.Collection;

/**
 * 持有原模组全局延时队列引用，以便在服务器停止时安全清理跨世界残留闭包。
 */
public final class WorkQueueHolder {
    private static volatile Collection<AbstractMap.SimpleEntry<Runnable, Integer>> queueRef;

    private WorkQueueHolder() {
    }

    public static void bind(Collection<AbstractMap.SimpleEntry<Runnable, Integer>> queue) {
        queueRef = queue;
    }

    public static void clearOnServerStopped() {
        Collection<AbstractMap.SimpleEntry<Runnable, Integer>> ref = queueRef;
        if (ref != null) {
            ref.clear();
        }
    }
}
