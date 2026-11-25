package com.hp.borninchaosoptimizer.mixin.procedure;

import com.hp.borninchaosoptimizer.util.BicCrossCheckHelper;
import net.mcreator.borninchaosv.procedures.CharmofEnduranceproProcedure;
import net.mcreator.borninchaosv.procedures.CharmofFuryproProcedure;
import net.mcreator.borninchaosv.procedures.CharmofPowerPriShchielchkiePKMProcedure;
import net.mcreator.borninchaosv.procedures.CharmofResistanceproProcedure;
import net.mcreator.borninchaosv.procedures.CharmofStealthproProcedure;
import net.mcreator.borninchaosv.procedures.FrostbittenBladePriShchielchkiePKMProcedure;
import net.mcreator.borninchaosv.procedures.PumpkinStaffAtacProcedure;
import net.mcreator.borninchaosv.procedures.StaffoftheSummonerPriShchielchkiePravoiKnopkoiMyshiNaBlokieProcedure;
import net.mcreator.borninchaosv.procedures.SweetSwordPriShchielchkiePKMProcedure;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * 补全带坐标与物品栈参数的 9 个主动技能过程（护符、霜噬之刃、甜食剑、召唤法杖、南瓜法杖）的耐久扣减上下文：
 * 修复创造模式误扣耐久以及物品损毁时无破碎动画与音效的问题。
 */
@Mixin({
        CharmofEnduranceproProcedure.class,
        CharmofFuryproProcedure.class,
        CharmofPowerPriShchielchkiePKMProcedure.class,
        CharmofResistanceproProcedure.class,
        CharmofStealthproProcedure.class,
        FrostbittenBladePriShchielchkiePKMProcedure.class,
        PumpkinStaffAtacProcedure.class,
        StaffoftheSummonerPriShchielchkiePravoiKnopkoiMyshiNaBlokieProcedure.class,
        SweetSwordPriShchielchkiePKMProcedure.class
})
public abstract class ItemDurabilityContextMixin {
    @Redirect(
            method = "execute(Lnet/minecraft/world/level/LevelAccessor;DDDLnet/minecraft/world/entity/Entity;Lnet/minecraft/world/item/ItemStack;)V",
            remap = false,
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;hurt(ILnet/minecraft/util/RandomSource;Lnet/minecraft/server/level/ServerPlayer;)Z", remap = true)
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
}
