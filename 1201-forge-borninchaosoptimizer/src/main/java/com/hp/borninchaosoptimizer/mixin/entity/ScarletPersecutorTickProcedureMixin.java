package com.hp.borninchaosoptimizer.mixin.entity;

import com.hp.borninchaosoptimizer.config.BicOptimizerConfig;
import net.mcreator.borninchaosv.procedures.ScarletpersecutorPriObnovlieniiTaktaSushchnostiProcedure;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 优化 ScarletpersecutorPriObnovlieniiTaktaSushchnostiProcedure：
 * 1. 修复原代码在客户端侧每 tick 调用 isInWall() 与本地 teleportTo() 导致客户端位置拉扯闪现的问题；
 * 2. 服务端按实体 ID 错峰每 4 tick 执行一次脱困检查（原版 baseTick 已处理常规窒息判定），降低碰撞箱求交开销。
 */
@Mixin(value = ScarletpersecutorPriObnovlieniiTaktaSushchnostiProcedure.class, remap = false)
public abstract class ScarletPersecutorTickProcedureMixin {
    @Inject(method = "execute", at = @At("HEAD"), cancellable = true)
    private static void bic$serverSideThrottledUnstuck(double x, double y, double z, Entity entity, CallbackInfo ci) {
        if (!BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.enableGlobalFixes)) {
            return;
        }
        ci.cancel();
        if (entity == null || entity.level().isClientSide()) {
            return;
        }
        if (((entity.tickCount + entity.getId()) & 3) != 0) {
            return;
        }
        if (entity.isInWall()) {
            entity.teleportTo(x + 1.0, y + 1.0, z);
            if (entity instanceof ServerPlayer sp) {
                sp.connection.teleport(x + 1.0, y + 1.0, z, entity.getYRot(), entity.getXRot());
            }
        }
    }
}
