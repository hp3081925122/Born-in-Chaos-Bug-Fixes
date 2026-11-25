package com.hp.borninchaosoptimizer.api;

import net.minecraft.resources.ResourceLocation;

/**
 * 为生于混沌全部 83 个动画实体提供统一的 O(1) 动画状态读写与贴图路径缓存契约，
 * 替代原模组 EntityAnimationFactory 对全服每个生物执行 83 次 instanceof 的线性扫描，
 * 修复服务端同 tick 立即清除同步动画导致的客户端吞动画问题，并消除每帧重复构造贴图 ResourceLocation。
 */
public interface BicAnimatableEntity {
    void bic$handleAnimationSyncTick(boolean clientSide);

    ResourceLocation bic$getCachedTextureLocation();
}
