package com.hp.borninchaosoptimizer.util;

import java.util.Calendar;

/**
 * 缓存系统月与日（每 60 秒刷新一次），消除生于混沌节日事件在每次实体生成和死亡时
 * 重复调用多达 15 次 Calendar.getInstance() 造成的时区查询与日历数组分配。
 */
public final class SeasonalDateCache {
    private static volatile long nextRefreshMillis = 0L;
    private static volatile int cachedMonth = -1;
    private static volatile int cachedDay = -1;

    private SeasonalDateCache() {
    }

    private static void refreshIfNeeded() {
        long now = System.currentTimeMillis();
        if (now >= nextRefreshMillis || cachedMonth < 0) {
            Calendar cal = Calendar.getInstance();
            cachedMonth = cal.get(Calendar.MONTH);
            cachedDay = cal.get(Calendar.DAY_OF_MONTH);
            nextRefreshMillis = now + 60_000L;
        }
    }

    public static boolean isHalloweenWindow() {
        refreshIfNeeded();
        int m = cachedMonth;
        int d = cachedDay;
        return (m == Calendar.OCTOBER && d >= 25 && d <= 31)
                || (m == Calendar.NOVEMBER && d >= 1 && d <= 7);
    }

    public static boolean isChaoticSpringWindow() {
        refreshIfNeeded();
        return cachedMonth == Calendar.APRIL && cachedDay >= 10 && cachedDay <= 22;
    }

    public static boolean isChillingHorrorWindow() {
        refreshIfNeeded();
        int m = cachedMonth;
        int d = cachedDay;
        return (m == Calendar.DECEMBER && d >= 21 && d <= 31)
                || (m == Calendar.JANUARY && d >= 1 && d <= 10);
    }

    /**
     * 梦魇潜行者情人节限定贴图时间窗口（2 月 12 日至 2 月 20 日，含首尾）。
     */
    public static boolean isNightmareLoverTextureWindow() {
        refreshIfNeeded();
        return cachedMonth == Calendar.FEBRUARY && cachedDay >= 12 && cachedDay <= 20;
    }

    /**
     * 梦魇潜行者死亡掉落巧克力之心时间窗口（2 月 13 日至 2 月 19 日，对应原逻辑 >= 13 且 < 20）。
     */
    public static boolean isChocolateHeartDropWindow() {
        refreshIfNeeded();
        return cachedMonth == Calendar.FEBRUARY && cachedDay >= 13 && cachedDay < 20;
    }
}
