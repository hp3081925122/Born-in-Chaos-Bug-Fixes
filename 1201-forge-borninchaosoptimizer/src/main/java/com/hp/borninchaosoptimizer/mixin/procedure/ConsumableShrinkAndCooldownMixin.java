package com.hp.borninchaosoptimizer.mixin.procedure;

import com.hp.borninchaosoptimizer.config.BicOptimizerConfig;
import com.hp.borninchaosoptimizer.util.BicCrossCheckHelper;
import net.mcreator.borninchaosv.init.BornInChaosV1ModItems;
import net.mcreator.borninchaosv.procedures.BagofCandyPriShchielchkiePKMProcedure;
import net.mcreator.borninchaosv.procedures.BoneHeartPriShchielchkiePKMProcedure;
import net.mcreator.borninchaosv.procedures.CreepyGiftPriShchielchkiePKMProcedure;
import net.mcreator.borninchaosv.procedures.DarkAtriumPriShchielchkiePKMProcedure;
import net.mcreator.borninchaosv.procedures.KrampussBagPriShchielchkiePKMProcedure;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 修复手持右键消耗品过程（带 ItemStack 参数组）：
 * 1. 单格消耗品先调用 shrink(1) 后再 addCooldown 导致空气进冷却的 0 冷却连点漏洞；
 * 2. 创造模式玩家使用消耗品时误扣数量；
 * 3. 诡异礼盒副手使用时漏播副手挥动动画。
 */
@Mixin({
        BagofCandyPriShchielchkiePKMProcedure.class,
        BoneHeartPriShchielchkiePKMProcedure.class,
        CreepyGiftPriShchielchkiePKMProcedure.class,
        DarkAtriumPriShchielchkiePKMProcedure.class,
        KrampussBagPriShchielchkiePKMProcedure.class
})
public abstract class ConsumableShrinkAndCooldownMixin {
    @Inject(
            method = "execute(Lnet/minecraft/world/level/LevelAccessor;DDDLnet/minecraft/world/entity/Entity;Lnet/minecraft/world/item/ItemStack;)V",
            remap = false,
            at = @At("HEAD")
    )
    private static void bic$fixCreepyGiftOffhandSwing(
            LevelAccessor world,
            double x,
            double y,
            double z,
            Entity entity,
            ItemStack itemstack,
            CallbackInfo ci
    ) {
        if (!BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.enableGlobalFixes)
                || !BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.fixConsumableCooldownAndCreative)) {
            return;
        }
        if (entity instanceof LivingEntity living && itemstack.getItem() == BornInChaosV1ModItems.CREEPY_GIFT.get()) {
            if (living.getMainHandItem().getItem() != BornInChaosV1ModItems.CREEPY_GIFT.get()
                    && living.getOffhandItem().getItem() == BornInChaosV1ModItems.CREEPY_GIFT.get()) {
                living.swing(InteractionHand.OFF_HAND, true);
            }
        }
    }

    @Redirect(
            method = "execute(Lnet/minecraft/world/level/LevelAccessor;DDDLnet/minecraft/world/entity/Entity;Lnet/minecraft/world/item/ItemStack;)V",
            remap = false,
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;shrink(I)V", remap = true)
    )
    private static void bic$shrinkItemUse(
            ItemStack stack,
            int amount,
            LevelAccessor world,
            double x,
            double y,
            double z,
            Entity entity,
            ItemStack itemstack
    ) {
        BicCrossCheckHelper.safeShrinkConsumable(stack, amount, entity);
    }

    @Redirect(
            method = "execute(Lnet/minecraft/world/level/LevelAccessor;DDDLnet/minecraft/world/entity/Entity;Lnet/minecraft/world/item/ItemStack;)V",
            remap = false,
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/ItemCooldowns;addCooldown(Lnet/minecraft/world/item/Item;I)V",
                    remap = true
            ),
            require = 0
    )
    private static void bic$restoreActualItemCooldown(ItemCooldowns cooldowns, Item item, int ticks) {
        BicCrossCheckHelper.restoreActualItemCooldown(cooldowns, item, ticks);
    }

    @Inject(
            method = "execute(Lnet/minecraft/world/level/LevelAccessor;DDDLnet/minecraft/world/entity/Entity;Lnet/minecraft/world/item/ItemStack;)V",
            remap = false,
            at = @At("RETURN")
    )
    private static void bic$clearThreadLocal(CallbackInfo ci) {
        BicCrossCheckHelper.clearConsumedItemThreadLocal();
    }
}
