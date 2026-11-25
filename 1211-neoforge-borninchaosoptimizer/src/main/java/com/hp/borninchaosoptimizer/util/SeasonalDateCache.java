package com.hp.borninchaosoptimizer.util;

import java.time.LocalDate;

/**
 * 季节性事件公历日期缓存，避免每次生物生成或死亡时重复创建多个 Calendar 与 TimeZone 对象。
 */
public final class SeasonalDateCache {
    private static volatile long lastCheckMillis = 0L;
    private static volatile int cachedZeroBasedMonth = 0;
    private static volatile int cachedDayOfMonth = 1;

    private SeasonalDateCache() {
    }

    private static void refreshIfNeeded() {
        long now = System.currentTimeMillis();
        if (now - lastCheckMillis > 60_000L) {
            LocalDate today = LocalDate.now();
            // 原模组使用 Calendar.MONTH（0 基：0=一月，3=四月，9=十月，10=十一月，11=十二月）
            cachedZeroBasedMonth = today.getMonthValue() - 1;
            cachedDayOfMonth = today.getDayOfMonth();
            lastCheckMillis = now;
        }
    }

    public static boolean isChaoticSpringDate() {
        refreshIfNeeded();
        return cachedZeroBasedMonth == 3 && cachedDayOfMonth >= 10 && cachedDayOfMonth <= 22;
    }

    public static boolean isHalloweenDate() {
        refreshIfNeeded();
        return (cachedZeroBasedMonth == 9 && cachedDayOfMonth >= 25 && cachedDayOfMonth <= 31)
                || (cachedZeroBasedMonth == 10 && cachedDayOfMonth >= 1 && cachedDayOfMonth <= 7);
    }

    public static boolean isChillingHorrorDate() {
        refreshIfNeeded();
        return (cachedZeroBasedMonth == 11 && cachedDayOfMonth >= 21 && cachedDayOfMonth <= 31)
                || (cachedZeroBasedMonth == 0 && cachedDayOfMonth >= 1 && cachedDayOfMonth <= 10);
    }

    public static boolean isValentinesDate() {
        refreshIfNeeded();
        return cachedZeroBasedMonth == 1 && cachedDayOfMonth >= 13 && cachedDayOfMonth <= 20;
    }
}
