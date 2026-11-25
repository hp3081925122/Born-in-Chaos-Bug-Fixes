package com.hp.borninchaosoptimizer.mixin.procedure;

import com.hp.borninchaosoptimizer.util.BicCrossCheckHelper;
import net.mcreator.borninchaosv.procedures.FelLampPriShchielchkiePravoiKnopkoiMyshiNaBlokieProcedure;
import net.mcreator.borninchaosv.procedures.LordPumpkinheadsLampclicProcedure;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * 修复邪能魔灯与南瓜领主魔灯右键方块召唤过程在创造模式误扣物品，以及主手堆叠大于 1 时直接覆盖抹除整组剩余魔灯的问题。
 */
@Mixin({
        FelLampPriShchielchkiePravoiKnopkoiMyshiNaBlokieProcedure.class,
        LordPumpkinheadsLampclicProcedure.class
})
public abstract class FelLampStackPreserveMixin {
    @Redirect(
            method = "execute(Lnet/minecraft/world/level/LevelAccessor;DDDLnet/minecraft/world/entity/Entity;)V",
            remap = false,
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;shrink(I)V", remap = true)
    )
    private static void bic$shrinkLampUse(
            ItemStack stack,
            int amount,
            LevelAccessor world,
            double x,
            double y,
            double z,
            Entity entity
    ) {
        BicCrossCheckHelper.shrinkLampItem(stack, amount, entity);
    }

    @Redirect(
            method = "execute(Lnet/minecraft/world/level/LevelAccessor;DDDLnet/minecraft/world/entity/Entity;)V",
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
