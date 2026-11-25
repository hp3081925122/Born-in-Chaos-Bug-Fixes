package com.hp.borninchaosoptimizer.mixin.procedure;

import com.hp.borninchaosoptimizer.util.BicCrossCheckHelper;
import net.mcreator.borninchaosv.procedures.*;
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
 * 补全生于混沌其余 17 个耐久扣减过程（护符、仪式匕首、焦木去皮、霜噬之刃、甜食剑、号角、法杖、暴食鱼/窃命者破盾、桦树枝等）的持有者上下文：
 * 修复创造模式误扣耐久以及物品/盾牌损毁时无破碎动画与音效的问题。
 */
@Mixin({
        BirchBranchesPriUdariePoSushchnostiInstrumientomProcedure.class,
        CharmofEnduranceproProcedure.class,
        CharmofFuryproProcedure.class,
        CharmofPowerPriShchielchkiePKMProcedure.class,
        CharmofResistanceproProcedure.class,
        CharmofStealthproProcedure.class,
        DarkRitualDaggerPProcedure.class,
        EvilCarvedPumpkinPProcedure.class,
        FragileScorchedPlanksPProcedure.class,
        FrostbittenBladePriShchielchkiePKMProcedure.class,
        GluttonfishatackProcedure.class,
        HewnScorchedLogPProcedure.class,
        LifestealerTFAtackProcedure.class,
        PumpkinStaffAtacProcedure.class,
        StaffoftheSummonerPriShchielchkiePravoiKnopkoiMyshiNaBlokieProcedure.class,
        StormcallersHornPriZaviershieniiIspolzovaniiaProcedure.class,
        SweetSwordPriShchielchkiePKMProcedure.class
})
public abstract class ItemDurabilityContextMixin {
    @Redirect(
            method = "execute(Lnet/minecraft/world/level/LevelAccessor;DDDLnet/minecraft/world/entity/Entity;Lnet/minecraft/world/item/ItemStack;)V",
            remap = false,
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;hurt(ILnet/minecraft/util/RandomSource;Lnet/minecraft/server/level/ServerPlayer;)Z", remap = true),
            require = 0
    )
    private static boolean bic$hurtWorldXyzEntityStack(
            ItemStack stack,
            int amount,
            RandomSource random,
            ServerPlayer player,
            LevelAccessor world,
            double x,
            double y,
            double z,
            Entity entity,
            ItemStack itemstack
    ) {
        return BicCrossCheckHelper.redirectItemStackHurt(stack, amount, random, player, entity);
    }

    @Redirect(
            method = "execute(Lnet/minecraft/world/level/LevelAccessor;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/item/ItemStack;)V",
            remap = false,
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;hurt(ILnet/minecraft/util/RandomSource;Lnet/minecraft/server/level/ServerPlayer;)Z", remap = true),
            require = 0
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

    @Redirect(
            method = "execute(Lnet/minecraft/world/level/LevelAccessor;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/item/ItemStack;)V",
            remap = false,
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;hurt(ILnet/minecraft/util/RandomSource;Lnet/minecraft/server/level/ServerPlayer;)Z", remap = true),
            require = 0
    )
    private static boolean bic$hurtWorldTargetSourceStack(
            ItemStack stack,
            int amount,
            RandomSource random,
            ServerPlayer player,
            LevelAccessor world,
            Entity target,
            Entity sourceentity,
            ItemStack itemstack
    ) {
        return BicCrossCheckHelper.redirectItemStackHurt(stack, amount, random, player, sourceentity);
    }

    @Redirect(
            method = "execute(Lnet/minecraftforge/eventbus/api/Event;Lnet/minecraft/world/level/LevelAccessor;DDDLnet/minecraft/world/entity/Entity;)V",
            remap = false,
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;hurt(ILnet/minecraft/util/RandomSource;Lnet/minecraft/server/level/ServerPlayer;)Z", remap = true),
            require = 0
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

    @Redirect(
            method = "execute(Lnet/minecraftforge/eventbus/api/Event;Lnet/minecraft/world/level/LevelAccessor;DDDLnet/minecraft/world/entity/Entity;Lnet/minecraft/world/entity/Entity;)V",
            remap = false,
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;hurt(ILnet/minecraft/util/RandomSource;Lnet/minecraft/server/level/ServerPlayer;)Z", remap = true),
            require = 0
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
        Entity holder = ((Object) stack == (entity instanceof net.minecraft.world.entity.LivingEntity l ? l.getUseItem() : null))
                ? entity
                : sourceentity;
        return BicCrossCheckHelper.redirectItemStackHurt(stack, amount, random, player, holder);
    }

    @Redirect(
            method = "execute(Lnet/minecraftforge/eventbus/api/Event;Lnet/minecraft/world/level/LevelAccessor;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/entity/Entity;)V",
            remap = false,
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;hurt(ILnet/minecraft/util/RandomSource;Lnet/minecraft/server/level/ServerPlayer;)Z", remap = true),
            require = 0
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
