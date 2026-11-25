package com.hp.borninchaosoptimizer.util;

/**
 * 生于混沌同步动画实体统一访问接口，用于替代 83 次顺序 instanceof 判断，并修复服务端同刻覆盖吞动画问题。
 */
public interface IBicSyncedAnimatable {
    String getSyncedAnimation();

    void setAnimation(String animation);

    void bic$setAnimationProcedure(String animation);

    void bic$handleAnimationSyncTick(boolean clientSide);

    String getTexture();
}
