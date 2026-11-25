package com.hp.borninchaosoptimizer.mixin.procedure;

import com.hp.borninchaosoptimizer.util.BicCrossCheckHelper;
import net.mcreator.borninchaosv.procedures.LifestealerTFAtackProcedure;
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
 * 补全窃命者真身破盾攻击过程的盾牌耐久扣减持有者上下文。
 */
@Mixin(LifestealerTFAtackProcedure.class)
public abstract class ItemDurabilityEventCombatMixin {
    @Redirect(
            method = "execute(Lnet/minecraftforge/eventbus/api/Event;Lnet/minecraft/world/level/LevelAccessor;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/entity/Entity;)V",
            remap = false,
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;hurt(ILnet/minecraft/util/RandomSource;Lnet/minecraft/server/level/ServerPlayer;)Z", remap = true)
    )
    private static boolean bic$hurtEventTargetSource(
            ItemStack stack,
            int amount,
            RandomSource random,
            ServerPlayer player,
            Event event,
            LevelAccessor world,
            Entity entity,
            Entity sourceentity
    ) {
        return BicCrossCheckHelper.redirectItemStackHurt(stack, amount, random, player, entity);
    }
}
