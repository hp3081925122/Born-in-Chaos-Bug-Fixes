package com.hp.borninchaosoptimizer.mixin.procedure;

import com.hp.borninchaosoptimizer.util.BicCrossCheckHelper;
import net.mcreator.borninchaosv.procedures.EvilCarvedPumpkinPProcedure;
import net.mcreator.borninchaosv.procedures.FragileScorchedPlanksPProcedure;
import net.mcreator.borninchaosv.procedures.HewnScorchedLogPProcedure;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelAccessor;
import net.minecraftforge.eventbus.api.Event;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * 补全带事件参数的方块加工交互过程（邪恶雕刻南瓜、焦黑脆弱木板、焦黑去皮原木）的工具耐久扣减持有者上下文。
 */
@Mixin({
        EvilCarvedPumpkinPProcedure.class,
        FragileScorchedPlanksPProcedure.class,
        HewnScorchedLogPProcedure.class
})
public abstract class ItemDurabilityEventBlockMixin {
    @Redirect(
            method = "execute(Lnet/minecraftforge/eventbus/api/Event;Lnet/minecraft/world/level/LevelAccessor;DDDLnet/minecraft/world/entity/Entity;)V",
            remap = false,
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;hurt(ILnet/minecraft/util/RandomSource;Lnet/minecraft/server/level/ServerPlayer;)Z", remap = true)
    )
    private static boolean bic$hurtEventBlockEntity(
            ItemStack stack,
            int amount,
            RandomSource random,
            ServerPlayer player,
            Event event,
            LevelAccessor world,
            double x,
            double y,
            double z,
            Entity entity
    ) {
        return BicCrossCheckHelper.redirectItemStackHurt(stack, amount, random, player, entity);
    }
}
