package com.hp.borninchaosoptimizer.mixin.procedure;

import com.hp.borninchaosoptimizer.config.BicOptimizerConfig;
import com.hp.borninchaosoptimizer.util.BicCrossCheckHelper;
import net.mcreator.borninchaosv.init.BornInChaosV1ModItems;
import net.mcreator.borninchaosv.procedures.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.LevelAccessor;
import net.minecraftforge.eventbus.api.Event;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 修复消耗品与喂食交互过程的三类同源缺陷：
 * 1. 单格消耗品先调用 shrink(1) 导致物品栈变为空栈（Items.AIR），随后 addCooldown 将空气加入冷却而非真实消耗品，造成单格 0 冷却无限连点漏洞；
 * 2. 创造模式玩家使用消耗品或喂食生物时仍误扣物品数量；
 * 3. 诡异礼盒副手使用时误重复检查主手物品导致副手无挥手动画。
 */
@Mixin({
        BagofCandyPriShchielchkiePKMProcedure.class,
        BoneHeartPriShchielchkiePKMProcedure.class,
        CreepyGiftPriShchielchkiePKMProcedure.class,
        DarkAtriumPriShchielchkiePKMProcedure.class,
        KrampussBagPriShchielchkiePKMProcedure.class,
        PumpkinSpiritPriShchielchkiePKMPoSushchnostiProcedure.class,
        EtherealSpiritPriShchielchkiePravoiKnopkoiMyshiNaBlokieProcedure.class,
        SeaTerrorStomachPriShchielchkiePKMProcedure.class,
        DreadHoundPriNazhatiiPravoiKnopkiNaSushchnostProcedure.class,
        SwarmerPreparationProcedure.class
})
public abstract class ConsumableShrinkAndCooldownMixin {
    @Unique
    private static final ThreadLocal<Item> BIC_CONSUMED_ITEM = new ThreadLocal<>();

    @Inject(
            method = "execute(Lnet/minecraft/world/level/LevelAccessor;DDDLnet/minecraft/world/entity/Entity;Lnet/minecraft/world/item/ItemStack;)V",
            remap = false,
            at = @At("HEAD"),
            require = 0
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
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;shrink(I)V", remap = true),
            require = 0
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
        bic$safeShrink(stack, amount, entity);
    }

    @Redirect(
            method = "execute(Lnet/minecraft/world/level/LevelAccessor;DDDLnet/minecraft/world/entity/Entity;)V",
            remap = false,
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;shrink(I)V", remap = true),
            require = 0
    )
    private static void bic$shrinkBlockUse(
            ItemStack stack,
            int amount,
            LevelAccessor world,
            double x,
            double y,
            double z,
            Entity entity
    ) {
        bic$safeShrink(stack, amount, entity);
    }

    @Redirect(
            method = "execute(Lnet/minecraft/world/level/LevelAccessor;DDDLnet/minecraft/world/entity/Entity;Lnet/minecraft/world/entity/Entity;)V",
            remap = false,
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;shrink(I)V", remap = true),
            require = 0
    )
    private static void bic$shrinkEntityInteractDirect(
            ItemStack stack,
            int amount,
            LevelAccessor world,
            double x,
            double y,
            double z,
            Entity target,
            Entity sourceentity
    ) {
        bic$safeShrink(stack, amount, sourceentity);
    }

    @Redirect(
            method = "execute(Lnet/minecraftforge/eventbus/api/Event;Lnet/minecraft/world/level/LevelAccessor;DDDLnet/minecraft/world/entity/Entity;Lnet/minecraft/world/entity/Entity;)V",
            remap = false,
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;shrink(I)V", remap = true),
            require = 0
    )
    private static void bic$shrinkEntityInteractEvent(
            ItemStack stack,
            int amount,
            Event event,
            LevelAccessor world,
            double x,
            double y,
            double z,
            Entity target,
            Entity sourceentity
    ) {
        bic$safeShrink(stack, amount, sourceentity);
    }

    @Redirect(
            method = "execute*",
            remap = false,
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/ItemCooldowns;addCooldown(Lnet/minecraft/world/item/Item;I)V",
                    remap = true
            ),
            require = 0
    )
    private static void bic$restoreActualItemCooldown(ItemCooldowns cooldowns, Item item, int ticks) {
        Item captured = BIC_CONSUMED_ITEM.get();
        BIC_CONSUMED_ITEM.remove();
        Item realItem = (item == Items.AIR && captured != null && captured != Items.AIR) ? captured : item;
        if (realItem != Items.AIR) {
            cooldowns.addCooldown(realItem, ticks);
        }
    }

    @Inject(method = "execute*", remap = false, at = @At("RETURN"), require = 0)
    private static void bic$clearThreadLocal(CallbackInfo ci) {
        BIC_CONSUMED_ITEM.remove();
    }

    @Unique
    private static void bic$safeShrink(ItemStack stack, int amount, Entity actor) {
        if (!BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.enableGlobalFixes)
                || !BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.fixConsumableCooldownAndCreative)) {
            stack.shrink(amount);
            return;
        }
        if (!stack.isEmpty()) {
            BIC_CONSUMED_ITEM.set(stack.getItem());
        }
        if (!BicCrossCheckHelper.isCreativeOrSpectator(actor)) {
            stack.shrink(amount);
        }
    }
}
