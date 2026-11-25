package com.hp.borninchaosoptimizer.mixin.procedure;

import com.hp.borninchaosoptimizer.config.BicOptimizerConfig;
import net.mcreator.borninchaosv.procedures.*;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * 统一节流尸蝇、水生怪、状态子药水与穿戴装备/手持武器的每 tick 短时药水刷新：
 * 当目标身上已有同等级或更高等级的相同药水且剩余时长高于安全余量阈值（>= 3~8 tick）时跳过重复 addEffect，
 * 在 100% 不断档且卸下装备后消失时间不变的前提下，消除每 tick 标记 effectsDirty 引发的全服药水同步包风暴。
 */
@Mixin({
        CorpseFlyappearanceProcedure.class,
        BoneBarrierKazhdyiTikVoVriemiaEffiektaProcedure.class,
        GazeOfTerrorKazhdyiTikVoVriemiaEffiektaProcedure.class,
        RabbitAgilityKazhdyiTikVoVriemiaEffiektaProcedure.class,
        StrangleholdKazhdyiTikVoVriemiaEffiektaProcedure.class,
        EvilometerKazhdyiTikVInvientarieProcedure.class,
        BroadswordDestructionKazhdyiTikVRukieProcedure.class,
        KillerRabbitEarsKazhdyiTikDliaShliemaProcedure.class,
        NightmareMantleoftheNightKazhdyiTikDliaShliemaProcedure.class,
        NightmareMantleoftheNightKazhdyiTikDliaNaghrudnikaProcedure.class,
        LordPumpkinheadsHatKazhdyiTikDliaShliemaProcedure.class
})
public abstract class ShortEffectRefreshThrottleMixin {
    @Redirect(
            method = "execute*",
            remap = false,
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/LivingEntity;addEffect(Lnet/minecraft/world/effect/MobEffectInstance;)Z",
                    remap = true
            ),
            require = 0
    )
    private static boolean bic$throttleShortSustainEffect(LivingEntity entity, MobEffectInstance incoming) {
        if (incoming != null
                && incoming.getDuration() <= 60
                && BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.enableGlobalFixes)
                && BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.optimizeSubEffectRefreshes)) {
            MobEffectInstance current = entity.getEffect(incoming.getEffect());
            int minRemaining = Math.max(3, incoming.getDuration() / 3);
            if (current != null
                    && current.getAmplifier() >= incoming.getAmplifier()
                    && current.getDuration() > minRemaining) {
                return false;
            }
        }
        return entity.addEffect(incoming);
    }
}
