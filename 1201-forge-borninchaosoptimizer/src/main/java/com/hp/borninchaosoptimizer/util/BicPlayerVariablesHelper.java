package com.hp.borninchaosoptimizer.util;

import net.mcreator.borninchaosv.network.BornInChaosV1ModVariables;

/**
 * 提供 1.20.1 Forge 只读空 PlayerVariables 共享单例，消除 14 个过程共 70 处
 * {@code .orElse(new PlayerVariables())} 在每帧物品渲染与事件触发时产生的短命垃圾对象分配。
 */
public final class BicPlayerVariablesHelper {
    public static final BornInChaosV1ModVariables.PlayerVariables EMPTY_READONLY =
            new BornInChaosV1ModVariables.PlayerVariables();

    private BicPlayerVariablesHelper() {
    }
}
