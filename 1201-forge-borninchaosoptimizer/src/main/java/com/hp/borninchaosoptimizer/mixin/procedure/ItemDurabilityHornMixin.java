package com.hp.borninchaosoptimizer.mixin.procedure;

import com.hp.borninchaosoptimizer.util.BicCrossCheckHelper;
import net.mcreator.borninchaosv.procedures.StormcallersHornPriZaviershieniiIspolzovaniiaProcedure;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * 补全唤雷者号角使用结束过程的耐久扣减持有者上下文，修复创造模式误扣耐久与损毁反馈缺失问题。
 */
@Mixin(StormcallersHornPriZaviershieniiIspolzovaniiaProcedure.class)
public abstract class ItemDurabilityHornMixin {
    @Redirect(
            method = "execute(Lnet/minecraft/world/level/LevelAccessor;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/item/ItemStack;)V",
            remap = false,
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;hurt(ILnet/minecraft/util/RandomSource;Lnet/minecraft/server/level/ServerPlayer;)Z", remap = true)
    )
    private static boolean bic$hurtWorldEntityStack(
            ItemStack stack,
            int amount,
            RandomSource random,
            ServerPlayer player,
            LevelAccessor world,
            Entity entity,
            ItemStack itemstack
    ) {
        return BicCrossCheckHelper.redirectItemStackHurt(stack, amount, random, player, entity);
    }
}
