package com.hp.borninchaosoptimizer.mixin;

import com.hp.borninchaosoptimizer.config.OptimizerConfig;
import com.hp.borninchaosoptimizer.util.SafeClientBridge;
import net.mcreator.borninchaosv.init.BornInChaosV1ModItems;
import net.mcreator.borninchaosv.procedures.BagofCandyPriShchielchkiePKMProcedure;
import net.mcreator.borninchaosv.procedures.BoneHeartPriShchielchkiePKMProcedure;
import net.mcreator.borninchaosv.procedures.CreepyGiftPriShchielchkiePKMProcedure;
import net.mcreator.borninchaosv.procedures.DarkAtriumPriShchielchkiePKMProcedure;
import net.mcreator.borninchaosv.procedures.DreadHoundPriNazhatiiPravoiKnopkiNaSushchnostProcedure;
import net.mcreator.borninchaosv.procedures.EtherealSpiritPriShchielchkiePravoiKnopkoiMyshiNaBlokieProcedure;
import net.mcreator.borninchaosv.procedures.KrampussBagPriShchielchkiePKMProcedure;
import net.mcreator.borninchaosv.procedures.PumpkinSpiritPriShchielchkiePKMPoSushchnostiProcedure;
import net.mcreator.borninchaosv.procedures.SeaTerrorStomachPriShchielchkiePKMProcedure;
import net.mcreator.borninchaosv.procedures.SwarmerPreparationProcedure;
import net.mcreator.borninchaosv.procedures.TransmutingElixirclicProcedure;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.LevelAccessor;
import net.neoforged.bus.api.Event;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 修复消耗品与喂食交互过程的三类共性缺陷：
 * 1. 单格消耗品先调用 shrink(1) 导致数量归零变成空气，紧接着 addCooldown(stack.getItem(), ...) 将空气加入冷却而非原物品（导致黑暗心房、骨之心、糖果袋、诡异礼盒、坎卜斯袋、干草块喂南瓜灵可以单格 0 冷却无限连点）；
 * 2. 创造模式玩家使用消耗品或喂食生物时仍然扣减物品数量；
 * 3. 诡异礼盒副手使用时误重复检查主手导致副手无挥手动画。
 */
@Mixin(value = {
        BagofCandyPriShchielchkiePKMProcedure.class,
        BoneHeartPriShchielchkiePKMProcedure.class,
        CreepyGiftPriShchielchkiePKMProcedure.class,
        DarkAtriumPriShchielchkiePKMProcedure.class,
        KrampussBagPriShchielchkiePKMProcedure.class,
        PumpkinSpiritPriShchielchkiePKMPoSushchnostiProcedure.class,
        EtherealSpiritPriShchielchkiePravoiKnopkoiMyshiNaBlokieProcedure.class,
        SeaTerrorStomachPriShchielchkiePKMProcedure.class,
        DreadHoundPriNazhatiiPravoiKnopkiNaSushchnostProcedure.class,
        SwarmerPreparationProcedure.class,
        TransmutingElixirclicProcedure.class
}, remap = false)
public abstract class ConsumableShrinkAndCooldownFixMixin {
    @Unique
    private static final ThreadLocal<Item> BIC_LAST_CONSUMED_ITEM = new ThreadLocal<>();

    @Inject(
            method = "execute(Lnet/minecraft/world/level/LevelAccessor;DDDLnet/minecraft/world/entity/Entity;Lnet/minecraft/world/item/ItemStack;)V",
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
        if (!OptimizerConfig.isEnabled(OptimizerConfig.FIX_CONSUMABLE_CREATIVE_AND_COOLDOWN)) {
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
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;shrink(I)V"),
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
        bic$applySafeShrink(stack, amount, entity);
    }

    @Redirect(
            method = "execute(Lnet/minecraft/world/level/LevelAccessor;DDDLnet/minecraft/world/entity/Entity;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;shrink(I)V"),
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
        bic$applySafeShrink(stack, amount, entity);
    }

    @Redirect(
            method = "execute(Lnet/minecraft/world/level/LevelAccessor;DDDLnet/minecraft/world/entity/Entity;Lnet/minecraft/world/entity/Entity;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;shrink(I)V"),
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
        bic$applySafeShrink(stack, amount, sourceentity);
    }

    @Redirect(
            method = "execute(Lnet/neoforged/bus/api/Event;Lnet/minecraft/world/level/LevelAccessor;DDDLnet/minecraft/world/entity/Entity;Lnet/minecraft/world/entity/Entity;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;shrink(I)V"),
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
        bic$applySafeShrink(stack, amount, sourceentity);
    }

    @Redirect(
            method = {
                    "execute(Lnet/minecraft/world/level/LevelAccessor;DDDLnet/minecraft/world/entity/Entity;Lnet/minecraft/world/item/ItemStack;)V",
                    "execute(Lnet/minecraft/world/level/LevelAccessor;DDDLnet/minecraft/world/entity/Entity;Lnet/minecraft/world/entity/Entity;)V"
            },
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemCooldowns;addCooldown(Lnet/minecraft/world/item/Item;I)V"),
            require = 0
    )
    private static void bic$fixCooldownItemAfterShrink(ItemCooldowns cooldowns, Item item, int ticks) {
        Item lastConsumed = BIC_LAST_CONSUMED_ITEM.get();
        BIC_LAST_CONSUMED_ITEM.remove();
        if (OptimizerConfig.isEnabled(OptimizerConfig.FIX_CONSUMABLE_CREATIVE_AND_COOLDOWN)
                && item == Items.AIR
                && lastConsumed != null
                && lastConsumed != Items.AIR) {
            cooldowns.addCooldown(lastConsumed, ticks);
            return;
        }
        cooldowns.addCooldown(item, ticks);
    }

    @Unique
    private static void bic$applySafeShrink(ItemStack stack, int amount, Entity user) {
        if (!OptimizerConfig.isEnabled(OptimizerConfig.FIX_CONSUMABLE_CREATIVE_AND_COOLDOWN)) {
            stack.shrink(amount);
            return;
        }
        BIC_LAST_CONSUMED_ITEM.set(stack.getItem());
        if (!SafeClientBridge.isCreative(user)) {
            stack.shrink(amount);
        }
    }
}
