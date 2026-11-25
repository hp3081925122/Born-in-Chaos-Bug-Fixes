package com.hp.borninchaosoptimizer.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * 暴露 GeoEntityRenderer 内部实体姿态矩阵变换的内部钩子接口，
 * 供稀疏发光层直接复用与本体完全一致的朝向、骑乘、睡眠与死亡旋转变换。
 */
@OnlyIn(Dist.CLIENT)
public interface BicGeoEntityRendererHook<T extends Entity> {
    void bic$pushAndApplyEntityPose(PoseStack poseStack, T entity, float partialTick);
}
