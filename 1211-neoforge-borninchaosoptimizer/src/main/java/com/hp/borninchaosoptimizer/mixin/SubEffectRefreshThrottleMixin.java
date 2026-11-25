package com.hp.borninchaosoptimizer.mixin;

import com.hp.borninchaosoptimizer.config.OptimizerConfig;
import com.hp.borninchaosoptimizer.util.EntityEffectHelper;
import net.mcreator.borninchaosv.procedures.BoneBarrierKazhdyiTikVoVriemiaEffiektaProcedure;
import net.mcreator.borninchaosv.procedures.GazeOfTerrorKazhdyiTikVoVriemiaEffiektaProcedure;
import net.mcreator.borninchaosv.procedures.RabbitAgilityKazhdyiTikVoVriemiaEffiektaProcedure;
import net.mcreator.borninchaosv.procedures.StonePersistenceKazhdyiTikVoVriemiaEffiektaProcedure;
import net.mcreator.borninchaosv.procedures.StrangleholdKazhdyiTikVoVriemiaEffiektaProcedure;
import net.mcreator.borninchaosv.procedures.WitherResistanceKazhdyiTikVoVriemiaEffiektaProcedure;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * 统一节流 6 种状态效果（骸骨屏障、恐惧凝视、狡兔敏捷、磐石坚毅、扼喉、凋零抗性）在每刻过程中无条件刷新的子效果与无条件移除调用，
 * 在 100% 保持效果无缝覆盖的同时消除每刻发包与事件总线风暴。
 */
@Mixin(value = {
        BoneBarrierKazhdyiTikVoVriemiaEffiektaProcedure.class,
        GazeOfTerrorKazhdyiTikVoVriemiaEffiektaProcedure.class,
        RabbitAgilityKazhdyiTikVoVriemiaEffiektaProcedure.class,
        StonePersistenceKazhdyiTikVoVriemiaEffiektaProcedure.class,
        StrangleholdKazhdyiTikVoVriemiaEffiektaProcedure.class,
        WitherResistanceKazhdyiTikVoVriemiaEffiektaProcedure.class
}, remap = false)
public abstract class SubEffectRefreshThrottleMixin {
    @Redirect(
            method = "execute",
            require = 0,
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/LivingEntity;addEffect(Lnet/minecraft/world/effect/MobEffectInstance;)Z"
            )
    )
    private static boolean bic$throttleSubEffectAdd(LivingEntity entity, MobEffectInstance instance) {
        if (!OptimizerConfig.isEnabled(OptimizerConfig.OPTIMIZE_EFFECT_TICK_SCANS)) {
            return entity.addEffect(instance);
        }
        int threshold = Math.max(2, Math.min(8, instance.getDuration() / 3));
        EntityEffectHelper.refreshPassiveEffect(entity, instance.getEffect(), instance.getDuration(), instance.getAmplifier(), threshold);
        return true;
    }

    @Redirect(
            method = "execute",
            require = 0,
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/LivingEntity;removeEffect(Lnet/minecraft/core/Holder;)Z"
            )
    )
    private static boolean bic$guardSubEffectRemove(LivingEntity entity, Holder<MobEffect> effect) {
        if (!OptimizerConfig.isEnabled(OptimizerConfig.OPTIMIZE_EFFECT_TICK_SCANS)) {
            return entity.removeEffect(effect);
        }
        if (entity.hasEffect(effect)) {
            return entity.removeEffect(effect);
        }
        return false;
    }
}
