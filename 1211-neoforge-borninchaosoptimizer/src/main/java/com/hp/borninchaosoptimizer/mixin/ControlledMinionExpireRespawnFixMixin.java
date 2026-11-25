package com.hp.borninchaosoptimizer.mixin;

import com.hp.borninchaosoptimizer.config.OptimizerConfig;
import net.mcreator.borninchaosv.procedures.ControlledBabySkeletonPriObnovlieniiTaktaSushchnostiProcedure;
import net.mcreator.borninchaosv.procedures.MrPumpkinPProcedure;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * 修复受控幼年骷髅与受控南瓜先生在抗性提升药水到期调用 discard() 后漏写 return，
 * 导致同一方法下方 if (isBaby()) 分支立刻原地刷出一只新个体而永不消失的缺陷。
 */
@Mixin(value = {
        ControlledBabySkeletonPriObnovlieniiTaktaSushchnostiProcedure.class,
        MrPumpkinPProcedure.class
}, remap = false)
public abstract class ControlledMinionExpireRespawnFixMixin {
    @Redirect(
            method = "execute",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;isBaby()Z")
    )
    private static boolean bic$preventRespawnAfterExpire(LivingEntity living) {
        if (OptimizerConfig.isEnabled(OptimizerConfig.FIX_BOSS_AND_MINION_DUPLICATE_SPAWN)) {
            if (living.level().isClientSide() || living.isRemoved() || !living.hasEffect(MobEffects.DAMAGE_RESISTANCE)) {
                return false;
            }
        }
        return living.isBaby();
    }
}
