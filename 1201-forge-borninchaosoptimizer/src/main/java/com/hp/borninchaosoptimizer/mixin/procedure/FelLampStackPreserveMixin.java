package com.hp.borninchaosoptimizer.mixin.procedure;

import com.hp.borninchaosoptimizer.config.BicOptimizerConfig;
import com.hp.borninchaosoptimizer.util.BicCrossCheckHelper;
import net.mcreator.borninchaosv.procedures.FelLampPriShchielchkiePravoiKnopkoiMyshiNaBlokieProcedure;
import net.mcreator.borninchaosv.procedures.LordPumpkinheadsLampclicProcedure;
import net.mcreator.borninchaosv.procedures.RidingFelsteedPriNazhatiiPravoiKnopkiNaSushchnostProcedure;
import net.mcreator.borninchaosv.procedures.RidinglordsfelsteedclickProcedure;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * 修复邪能魔灯、南瓜领主魔灯及坐骑交互收纳过程在创造模式误扣物品，以及主手堆叠大于 1 时直接 setItemInHand 覆盖抹除整组剩余魔灯的问题。
 */
@Mixin({
        FelLampPriShchielchkiePravoiKnopkoiMyshiNaBlokieProcedure.class,
        LordPumpkinheadsLampclicProcedure.class,
        RidingFelsteedPriNazhatiiPravoiKnopkiNaSushchnostProcedure.class,
        RidinglordsfelsteedclickProcedure.class
})
public abstract class FelLampStackPreserveMixin {
    @Redirect(
            method = "execute(Lnet/minecraft/world/level/LevelAccessor;DDDLnet/minecraft/world/entity/Entity;)V",
            remap = false,
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;shrink(I)V", remap = true),
            require = 0
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
        if (!BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.enableGlobalFixes)
                || !BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.fixConsumableCooldownAndCreative)
                || !BicCrossCheckHelper.isCreativeOrSpectator(entity)) {
            stack.shrink(amount);
        }
    }

    @Redirect(
            method = "execute(Lnet/minecraft/world/level/LevelAccessor;DDDLnet/minecraft/world/entity/Entity;Lnet/minecraft/world/entity/Entity;)V",
            remap = false,
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;shrink(I)V", remap = true),
            require = 0
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
        if (!BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.enableGlobalFixes)
                || !BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.fixConsumableCooldownAndCreative)
                || !BicCrossCheckHelper.isCreativeOrSpectator(sourceentity)) {
            stack.shrink(amount);
        }
    }

    @Redirect(
            method = "execute*",
            remap = false,
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/LivingEntity;setItemInHand(Lnet/minecraft/world/InteractionHand;Lnet/minecraft/world/item/ItemStack;)V",
                    remap = true
            ),
            require = 0
    )
    private static void bic$preserveRemainingLampStack(LivingEntity holder, InteractionHand hand, ItemStack resultStack) {
        if (!BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.enableGlobalFixes)
                || !BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.fixConsumableCooldownAndCreative)) {
            holder.setItemInHand(hand, resultStack);
            return;
        }
        ItemStack remaining = holder.getItemInHand(hand);
        if (remaining.isEmpty()) {
            holder.setItemInHand(hand, resultStack);
            return;
        }
        if (holder instanceof Player player) {
            if (!BicCrossCheckHelper.isCreativeOrSpectator(player) || !player.getInventory().contains(resultStack)) {
                if (!player.getInventory().add(resultStack)) {
                    player.drop(resultStack, false);
                }
            }
        } else {
            holder.setItemInHand(hand, resultStack);
        }
    }
}
