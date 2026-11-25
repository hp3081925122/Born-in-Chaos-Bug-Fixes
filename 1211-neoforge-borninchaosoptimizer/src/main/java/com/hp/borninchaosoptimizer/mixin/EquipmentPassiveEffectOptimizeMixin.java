package com.hp.borninchaosoptimizer.mixin;

import com.hp.borninchaosoptimizer.config.OptimizerConfig;
import com.hp.borninchaosoptimizer.util.BicResourceAndAnimCache;
import net.mcreator.borninchaosv.procedures.BroadswordDestructionKazhdyiTikVRukieProcedure;
import net.mcreator.borninchaosv.procedures.DamnedDemomansHatKazhdyiTikDliaShliemaProcedure;
import net.mcreator.borninchaosv.procedures.DarkMetalArmorSobytiieTaktovShliemaProcedure;
import net.mcreator.borninchaosv.procedures.EvilometerKazhdyiTikVInvientarieProcedure;
import net.mcreator.borninchaosv.procedures.KillerRabbitEarsKazhdyiTikDliaShliemaProcedure;
import net.mcreator.borninchaosv.procedures.LordPumpkinheadsHatKazhdyiTikDliaShliemaProcedure;
import net.mcreator.borninchaosv.procedures.NightmareMantleoftheNightKazhdyiTikDliaNaghrudnikaProcedure;
import net.mcreator.borninchaosv.procedures.NightmareMantleoftheNightKazhdyiTikDliaShliemaProcedure;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * 优化穿戴盔甲与手持武器每刻被动过程中的高频发包与事件广播开销：
 * 1. 仅在目标确实持有对应负面效果时才在服务端调用 removeEffect，消除暗金属四件套每刻 4 次与南瓜领主帽每刻 3 次空广播；
 * 2. 对 10~100 刻的维持型增益药水按剩余时长阈值节流刷新，消除每刻重算属性修饰符与每秒 20~80 个药水同步包的开销；
 * 3. 缓存每刻进度检查中的 ResourceLocation 解析结果。
 */
@Mixin(value = {
        DarkMetalArmorSobytiieTaktovShliemaProcedure.class,
        EvilometerKazhdyiTikVInvientarieProcedure.class,
        BroadswordDestructionKazhdyiTikVRukieProcedure.class,
        NightmareMantleoftheNightKazhdyiTikDliaShliemaProcedure.class,
        NightmareMantleoftheNightKazhdyiTikDliaNaghrudnikaProcedure.class,
        LordPumpkinheadsHatKazhdyiTikDliaShliemaProcedure.class,
        KillerRabbitEarsKazhdyiTikDliaShliemaProcedure.class,
        DamnedDemomansHatKazhdyiTikDliaShliemaProcedure.class
}, remap = false)
public abstract class EquipmentPassiveEffectOptimizeMixin {
    @Redirect(
            method = "execute",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/LivingEntity;removeEffect(Lnet/minecraft/core/Holder;)Z"
            ),
            require = 0
    )
    private static boolean bic$guardRemoveEffect(LivingEntity entity, Holder<MobEffect> effect) {
        if (!OptimizerConfig.isEnabled(OptimizerConfig.OPTIMIZE_EQUIPMENT_INVENTORY_TICKS)) {
            return entity.removeEffect(effect);
        }
        if (!entity.level().isClientSide() && entity.hasEffect(effect)) {
            return entity.removeEffect(effect);
        }
        return false;
    }

    @Redirect(
            method = "execute",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/LivingEntity;addEffect(Lnet/minecraft/world/effect/MobEffectInstance;)Z"
            ),
            require = 0
    )
    private static boolean bic$throttleShortPassiveEffect(LivingEntity entity, MobEffectInstance instance) {
        if (!OptimizerConfig.isEnabled(OptimizerConfig.OPTIMIZE_EQUIPMENT_INVENTORY_TICKS)) {
            return entity.addEffect(instance);
        }
        MobEffectInstance current = entity.getEffect(instance.getEffect());
        if (current != null && current.getAmplifier() >= instance.getAmplifier()) {
            int duration = instance.getDuration();
            int threshold = duration <= 10 ? 3 : (duration <= 30 ? 6 : 20);
            if (current.getDuration() > threshold) {
                return false;
            }
        }
        return entity.addEffect(instance);
    }

    @Redirect(
            method = "execute",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/resources/ResourceLocation;parse(Ljava/lang/String;)Lnet/minecraft/resources/ResourceLocation;"
            ),
            require = 0
    )
    private static ResourceLocation bic$cacheAdvancementLocation(String raw) {
        if (OptimizerConfig.isEnabled(OptimizerConfig.CACHE_RESOURCE_LOCATIONS_AND_SOUNDS)) {
            return BicResourceAndAnimCache.parseLocation(raw);
        }
        return ResourceLocation.parse(raw);
    }
}
