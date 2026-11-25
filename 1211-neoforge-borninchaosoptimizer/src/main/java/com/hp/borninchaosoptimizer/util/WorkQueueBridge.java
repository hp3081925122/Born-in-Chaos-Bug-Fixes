package com.hp.borninchaosoptimizer.util;

/**
 * 保存原模组主类实例引用以便在服务器关闭时清理静态延时任务队列。
 */
public final class WorkQueueBridge {
    private static volatile IBicWorkQueueAccess instance;

    private WorkQueueBridge() {
    }

    public static void bind(IBicWorkQueueAccess access) {
        instance = access;
    }

    public static void clearIfPresent() {
        IBicWorkQueueAccess current = instance;
        if (current != null) {
            current.bic$clearWorkQueue();
        }
    }
}
