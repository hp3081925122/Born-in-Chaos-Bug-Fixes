package com.hp.borninchaosoptimizer.util;

/**
 * 反伤事件重入守卫，防止 LivingAttackEvent 内调用 hurt() 触发双方互相反伤导致的 StackOverflowError。
 */
public final class RetaliationGuard {
    private static final ThreadLocal<Boolean> IN_RETALIATION = new ThreadLocal<>();

    private RetaliationGuard() {
    }

    public static boolean tryEnter() {
        if (Boolean.TRUE.equals(IN_RETALIATION.get())) {
            return false;
        }
        IN_RETALIATION.set(Boolean.TRUE);
        return true;
    }

    public static void exit() {
        IN_RETALIATION.remove();
    }
}
