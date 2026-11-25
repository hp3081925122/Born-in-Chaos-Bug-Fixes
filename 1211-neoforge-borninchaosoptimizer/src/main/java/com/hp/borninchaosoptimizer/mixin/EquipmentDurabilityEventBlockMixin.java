package com.hp.borninchaosoptimizer.mixin;

import com.hp.borninchaosoptimizer.util.Batch5ProcedureHelper;
import java.util.function.Consumer;
import net.mcreator.borninchaosv.procedures.EvilCarvedPumpkinPProcedure;
import net.mcreator.borninchaosv.procedures.FragileScorchedPlanksPProcedure;
import net.mcreator.borninchaosv.procedures.HewnScorchedLogPProcedure;
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
 * 为雕刻邪恶南瓜、去皮焦木、劈裂焦木板等方块右键交互过程补全剪刀/斧头耐久扣减上下文。
 */
@Mixin(value = {
        EvilCarvedPumpkinPProcedure.class,
        FragileScorchedPlanksPProcedure.class,
        HewnScorchedLogPProcedure.class
}, remap = false)
public abstract class EquipmentDurabilityEventBlockMixin {
    @Redirect(
            method = "execute(Lnet/neoforged/bus/api/Event;Lnet/minecraft/world/level/LevelAccessor;DDDLnet/minecraft/world/entity/Entity;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/ItemStack;hurtAndBreak(ILnet/minecraft/server/level/ServerLevel;Lnet/minecraft/server/level/ServerPlayer;Ljava/util/function/Consumer;)V"
            )
    )
    private static void bic$hurtWithContext(
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
            Entity entity
    ) {
        Batch5ProcedureHelper.hurtStackWithContext(stack, amount, level, originalPlayer, callback, entity);
    }
}
