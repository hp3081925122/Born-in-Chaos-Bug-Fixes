package com.hp.borninchaosoptimizer.mixin;

import com.hp.borninchaosoptimizer.util.Batch5ProcedureHelper;
import java.util.function.Consumer;
import net.mcreator.borninchaosv.procedures.DarkRitualDaggerPProcedure;
import net.mcreator.borninchaosv.procedures.StormcallersHornPriZaviershieniiIspolzovaniiaProcedure;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelAccessor;
import net.neoforged.bus.api.Event;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * 为黑暗仪式匕首献祭与唤雷号角吹奏过程补全耐久扣减持有者上下文。
 */
@Mixin(value = {
        DarkRitualDaggerPProcedure.class,
        StormcallersHornPriZaviershieniiIspolzovaniiaProcedure.class
}, remap = false)
public abstract class EquipmentDurabilitySpecialMixin {
    @Redirect(
            method = "execute(Lnet/neoforged/bus/api/Event;Lnet/minecraft/world/level/LevelAccessor;DDDLnet/minecraft/world/entity/Entity;Lnet/minecraft/world/entity/Entity;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/ItemStack;hurtAndBreak(ILnet/minecraft/server/level/ServerLevel;Lnet/minecraft/server/level/ServerPlayer;Ljava/util/function/Consumer;)V"
            ),
            require = 0
    )
    private static void bic$hurtRitualDagger(
            ItemStack stack,
            int amount,
            ServerLevel level,
            ServerPlayer originalPlayer,
            Consumer<Item> callback,
            Event event,
            LevelAccessor world,
            double x,
            double y,
            double z,
            Entity target,
            Entity sourceentity
    ) {
        Batch5ProcedureHelper.hurtStackWithContext(stack, amount, level, originalPlayer, callback, sourceentity);
    }

    @Redirect(
            method = "execute(Lnet/minecraft/world/level/LevelAccessor;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/item/ItemStack;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/ItemStack;hurtAndBreak(ILnet/minecraft/server/level/ServerLevel;Lnet/minecraft/server/level/ServerPlayer;Ljava/util/function/Consumer;)V"
            ),
            require = 0
    )
    private static void bic$hurtStormcallerHorn(
            ItemStack stack,
            int amount,
            ServerLevel level,
            ServerPlayer originalPlayer,
            Consumer<Item> callback,
            LevelAccessor world,
            Entity entity,
            ItemStack itemstack
    ) {
        Batch5ProcedureHelper.hurtStackWithContext(stack, amount, level, originalPlayer, callback, entity);
    }
}
