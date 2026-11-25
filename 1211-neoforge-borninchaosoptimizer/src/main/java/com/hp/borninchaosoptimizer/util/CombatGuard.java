package com.hp.borninchaosoptimizer.util;

/**
 * 反伤与链式伤害重入保护工具，防止与其他模组反伤机制形成无限递归调用栈溢出。
 */
public final class CombatGuard {
    private static final ThreadLocal<Boolean> IN_RETALIATION = ThreadLocal.withInitial(() -> Boolean.FALSE);

    private CombatGuard() {
    }

    public static boolean enterRetaliation() {
        if (Boolean.TRUE.equals(IN_RETALIATION.get())) {
            return false;
        }
        IN_RETALIATION.set(Boolean.TRUE);
        return true;
    }

    public static void exitRetaliation() {
        IN_RETALIATION.remove();
    }
}
