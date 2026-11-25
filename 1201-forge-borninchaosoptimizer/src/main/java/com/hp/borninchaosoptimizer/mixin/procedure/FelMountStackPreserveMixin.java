package com.hp.borninchaosoptimizer.mixin.procedure;

import com.hp.borninchaosoptimizer.util.BicCrossCheckHelper;
import net.mcreator.borninchaosv.procedures.RidingFelsteedPriNazhatiiPravoiKnopkiNaSushchnostProcedure;
import net.mcreator.borninchaosv.procedures.RidinglordsfelsteedclickProcedure;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * 修复邪能战马与领主邪能战马空灯收纳交互在创造模式误扣物品，以及主手堆叠大于 1 时覆盖抹除整组剩余魔灯的问题。
 */
@Mixin({
        RidingFelsteedPriNazhatiiPravoiKnopkiNaSushchnostProcedure.class,
        RidinglordsfelsteedclickProcedure.class
})
public abstract class FelMountStackPreserveMixin {
    @Redirect(
            method = "execute(Lnet/minecraft/world/level/LevelAccessor;DDDLnet/minecraft/world/entity/Entity;Lnet/minecraft/world/entity/Entity;)V",
            remap = false,
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;shrink(I)V", remap = true)
    )
    private static void bic$shrinkMountInteract(
            ItemStack stack,
            int amount,
            LevelAccessor world,
            double x,
            double y,
            double z,
            Entity mount,
            Entity sourceentity
    ) {
        BicCrossCheckHelper.shrinkLampItem(stack, amount, sourceentity);
    }

    @Redirect(
            method = "execute(Lnet/minecraft/world/level/LevelAccessor;DDDLnet/minecraft/world/entity/Entity;Lnet/minecraft/world/entity/Entity;)V",
            remap = false,
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/LivingEntity;setItemInHand(Lnet/minecraft/world/InteractionHand;Lnet/minecraft/world/item/ItemStack;)V",
                    remap = true
            )
    )
    private static void bic$preserveRemainingLampStack(LivingEntity holder, InteractionHand hand, ItemStack resultStack) {
        BicCrossCheckHelper.preserveRemainingLampStack(holder, hand, resultStack);
    }
}
