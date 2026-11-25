package com.hp.borninchaosoptimizer.mixin;

import com.hp.borninchaosoptimizer.config.OptimizerConfig;
import net.mcreator.borninchaosv.procedures.HoundTrapPriStolknovieniiSushchnostiSBlokomProcedure;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.LevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 修复捕兽夹碰撞过程的两大缺陷：
 * 1. 末尾 else if (!(entity instanceof Player)) 未限制 LivingEntity，导致地面掉落物、经验球或箭矢碰触时直接销毁物品并使捕兽夹误闭合；
 * 2. 未排除客户端执行，导致客户端提前生成未同步的幽灵闭合捕兽夹并触发重叠夹合音效。
 */
@Mixin(value = HoundTrapPriStolknovieniiSushchnostiSBlokomProcedure.class, remap = false)
public abstract class HoundTrapCollisionFixMixin {
    @Inject(method = "execute", at = @At("HEAD"), cancellable = true)
    private static void bic$ignoreNonLivingAndClient(LevelAccessor world, double x, double y, double z, Entity entity, CallbackInfo ci) {
        if (OptimizerConfig.isEnabled(OptimizerConfig.FIX_TRAP_AND_AOE_NON_LIVING_DAMAGE) && !(entity instanceof LivingEntity)) {
            ci.cancel();
            return;
        }
        if (OptimizerConfig.isEnabled(OptimizerConfig.FIX_CLIENT_SIDE_GHOST_MUTATIONS) && world.isClientSide()) {
            ci.cancel();
        }
    }
}
