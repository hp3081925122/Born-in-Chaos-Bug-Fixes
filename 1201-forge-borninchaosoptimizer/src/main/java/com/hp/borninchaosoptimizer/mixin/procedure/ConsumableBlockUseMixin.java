package com.hp.borninchaosoptimizer.mixin.procedure;

import com.hp.borninchaosoptimizer.util.BicCrossCheckHelper;
import net.mcreator.borninchaosv.procedures.EtherealSpiritPriShchielchkiePravoiKnopkoiMyshiNaBlokieProcedure;
import net.mcreator.borninchaosv.procedures.SeaTerrorStomachPriShchielchkiePKMProcedure;
import net.minecraft.world.entity.Entity;
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
 * 修复无物品栈参数的消耗品过程（空灵之魂、海魔胃袋）的单格冷却丢失与创造模式误扣问题。
 */
@Mixin({
        EtherealSpiritPriShchielchkiePravoiKnopkoiMyshiNaBlokieProcedure.class,
        SeaTerrorStomachPriShchielchkiePKMProcedure.class
})
public abstract class ConsumableBlockUseMixin {
    @Redirect(
            method = "execute(Lnet/minecraft/world/level/LevelAccessor;DDDLnet/minecraft/world/entity/Entity;)V",
            remap = false,
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;shrink(I)V", remap = true)
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
        BicCrossCheckHelper.safeShrinkConsumable(stack, amount, entity);
    }

    @Redirect(
            method = "execute(Lnet/minecraft/world/level/LevelAccessor;DDDLnet/minecraft/world/entity/Entity;)V",
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
            method = "execute(Lnet/minecraft/world/level/LevelAccessor;DDDLnet/minecraft/world/entity/Entity;)V",
            remap = false,
            at = @At("RETURN")
    )
    private static void bic$clearThreadLocal(CallbackInfo ci) {
        BicCrossCheckHelper.clearConsumedItemThreadLocal();
    }
}
