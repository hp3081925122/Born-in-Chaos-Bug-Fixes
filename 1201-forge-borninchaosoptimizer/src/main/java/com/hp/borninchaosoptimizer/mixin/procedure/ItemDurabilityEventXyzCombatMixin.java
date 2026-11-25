package com.hp.borninchaosoptimizer.mixin.procedure;

import com.hp.borninchaosoptimizer.util.BicCrossCheckHelper;
import net.mcreator.borninchaosv.procedures.DarkRitualDaggerPProcedure;
import net.mcreator.borninchaosv.procedures.GluttonfishatackProcedure;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelAccessor;
import net.minecraftforge.eventbus.api.Event;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * 补全带事件与坐标参数的战斗过程（黑暗仪式匕首与暴食鱼破盾）的耐久扣减持有者上下文。
 */
@Mixin({
        DarkRitualDaggerPProcedure.class,
        GluttonfishatackProcedure.class
})
public abstract class ItemDurabilityEventXyzCombatMixin {
    @Redirect(
            method = "execute(Lnet/minecraftforge/eventbus/api/Event;Lnet/minecraft/world/level/LevelAccessor;DDDLnet/minecraft/world/entity/Entity;Lnet/minecraft/world/entity/Entity;)V",
            remap = false,
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;hurt(ILnet/minecraft/util/RandomSource;Lnet/minecraft/server/level/ServerPlayer;)Z", remap = true)
    )
    private static boolean bic$hurtEventXyzTargetSource(
            ItemStack stack,
            int amount,
            RandomSource random,
            ServerPlayer player,
            Event event,
            LevelAccessor world,
            double x,
            double y,
            double z,
            Entity entity,
            Entity sourceentity
    ) {
        Entity holder = ((Object) stack == (entity instanceof LivingEntity l ? l.getUseItem() : null))
                ? entity
                : sourceentity;
        return BicCrossCheckHelper.redirectItemStackHurt(stack, amount, random, player, holder);
    }
}
