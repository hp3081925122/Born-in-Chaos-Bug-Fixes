package com.hp.borninchaosoptimizer.api;

/**
 * 标记当前实体是否正处于生于混沌实体的 baseTick 作用域内，
 * 以便仅拦截 MCreator 生成的每 tick 尾部冗余 refreshDimensions 调用，不影响外部模组主动刷新碰撞箱。
 */
public interface BicRefreshGuard {
    void bic$setInBicBaseTick(boolean value);
}
