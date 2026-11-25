package com.hp.borninchaosoptimizer.util;

/**
 * 线程局部药水生命周期上下文守卫：
 * 1. 记录 LivingEntity.onEffectUpdated 属性重挂窗口，避免活跃续期误触到期召唤；
 * 2. 记录 LivingEntity.onEffectRemoved 中剩余时长 > 0 的提前净化/清除窗口（如不死图腾、牛奶、指令清除），
 *    避免净化负面效果时反向触发惩罚刷怪。
 */
public final class EffectRenewalGuard {
    private static final ThreadLocal<Integer> REFRESH_DEPTH = ThreadLocal.withInitial(() -> 0);
    private static final ThreadLocal<Integer> EARLY_REMOVAL_DEPTH = ThreadLocal.withInitial(() -> 0);

    private EffectRenewalGuard() {
    }

    public static void enter() {
        REFRESH_DEPTH.set(REFRESH_DEPTH.get() + 1);
    }

    public static void exit() {
        int next = REFRESH_DEPTH.get() - 1;
        if (next <= 0) {
            REFRESH_DEPTH.remove();
        } else {
            REFRESH_DEPTH.set(next);
        }
    }

    public static boolean isRefreshing() {
        return REFRESH_DEPTH.get() > 0;
    }

    public static void enterEarlyRemoval() {
        EARLY_REMOVAL_DEPTH.set(EARLY_REMOVAL_DEPTH.get() + 1);
    }

    public static void exitEarlyRemoval() {
        int next = EARLY_REMOVAL_DEPTH.get() - 1;
        if (next <= 0) {
            EARLY_REMOVAL_DEPTH.remove();
        } else {
            EARLY_REMOVAL_DEPTH.set(next);
        }
    }

    public static boolean isEarlyRemoval() {
        return EARLY_REMOVAL_DEPTH.get() > 0;
    }
}
