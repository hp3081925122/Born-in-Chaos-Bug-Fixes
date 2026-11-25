package com.hp.borninchaosoptimizer.mixin.block;

import com.hp.borninchaosoptimizer.config.BicOptimizerConfig;
import net.mcreator.borninchaosv.procedures.HoundTrapPriStolknovieniiSushchnostiSBlokomProcedure;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.LevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 修复猎犬夹触发过程的两大问题：
 * 1. 限制仅在服务端权威执行，消除客户端提前放置闭合猎犬夹幽灵方块及双重音效；
 * 2. 排除地面战利品掉落物、经验球、箭矢等非生物实体，防止猎犬夹误闭合销毁玩家掉落装备。
 */
@Mixin(value = HoundTrapPriStolknovieniiSushchnostiSBlokomProcedure.class, remap = false)
public abstract class HoundTrapCollisionProcedureMixin {
    @Inject(method = "execute", at = @At("HEAD"), cancellable = true)
    private static void bicOpt$serverLivingOnly(LevelAccessor world, double x, double y, double z, Entity entity, CallbackInfo ci) {
        if (!BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.enableGlobalFixes)) {
            return;
        }
        if (entity == null) {
            ci.cancel();
            return;
        }
        if (world.isClientSide() && BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.optimizeStaticTrapBlockTicks)) {
            ci.cancel();
            return;
        }
        if (!(entity instanceof LivingEntity) && BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.fixNonLivingAoeAndTrapDamage)) {
            ci.cancel();
        }
    }
}
