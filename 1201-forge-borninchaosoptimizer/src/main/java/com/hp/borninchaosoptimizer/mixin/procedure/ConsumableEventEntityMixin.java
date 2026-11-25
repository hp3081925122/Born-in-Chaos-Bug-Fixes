package com.hp.borninchaosoptimizer.mixin.procedure;

import com.hp.borninchaosoptimizer.util.BicCrossCheckHelper;
import net.mcreator.borninchaosv.procedures.DreadHoundPriNazhatiiPravoiKnopkiNaSushchnostProcedure;
import net.mcreator.borninchaosv.procedures.SwarmerPreparationProcedure;
import net.mcreator.borninchaosv.procedures.TransmutingElixirclicProcedure;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelAccessor;
import net.minecraftforge.eventbus.api.Event;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 修复带事件参数的实体右键交互过程（恐惧猎犬驯服、虫群预处理、嬗变灵药转化）在单格消耗时的冷却丢失与创造模式误扣问题。
 */
@Mixin({
        DreadHoundPriNazhatiiPravoiKnopkiNaSushchnostProcedure.class,
        SwarmerPreparationProcedure.class,
        TransmutingElixirclicProcedure.class
})
public abstract class ConsumableEventEntityMixin {
    @Redirect(
            method = "execute(Lnet/minecraftforge/eventbus/api/Event;Lnet/minecraft/world/level/LevelAccessor;DDDLnet/minecraft/world/entity/Entity;Lnet/minecraft/world/entity/Entity;)V",
            remap = false,
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;shrink(I)V", remap = true)
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
        BicCrossCheckHelper.safeShrinkConsumable(stack, amount, sourceentity);
    }

    @Redirect(
            method = "execute(Lnet/minecraftforge/eventbus/api/Event;Lnet/minecraft/world/level/LevelAccessor;DDDLnet/minecraft/world/entity/Entity;Lnet/minecraft/world/entity/Entity;)V",
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
            method = "execute(Lnet/minecraftforge/eventbus/api/Event;Lnet/minecraft/world/level/LevelAccessor;DDDLnet/minecraft/world/entity/Entity;Lnet/minecraft/world/entity/Entity;)V",
            remap = false,
            at = @At("RETURN")
    )
    private static void bic$clearThreadLocal(CallbackInfo ci) {
        BicCrossCheckHelper.clearConsumedItemThreadLocal();
    }
}
