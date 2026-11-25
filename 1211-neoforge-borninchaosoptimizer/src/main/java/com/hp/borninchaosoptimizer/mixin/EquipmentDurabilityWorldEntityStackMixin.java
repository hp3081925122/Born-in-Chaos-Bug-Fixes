package com.hp.borninchaosoptimizer.mixin;

import com.hp.borninchaosoptimizer.util.Batch5ProcedureHelper;
import java.util.function.Consumer;
import net.mcreator.borninchaosv.procedures.CharmofEnduranceproProcedure;
import net.mcreator.borninchaosv.procedures.CharmofFuryproProcedure;
import net.mcreator.borninchaosv.procedures.CharmofPowerPriShchielchkiePKMProcedure;
import net.mcreator.borninchaosv.procedures.CharmofResistanceproProcedure;
import net.mcreator.borninchaosv.procedures.CharmofStealthproProcedure;
import net.mcreator.borninchaosv.procedures.FrostbittenBladePriShchielchkiePKMProcedure;
import net.mcreator.borninchaosv.procedures.PumpkinStaffAtacProcedure;
import net.mcreator.borninchaosv.procedures.StaffoftheSummonerPriShchielchkiePravoiKnopkoiMyshiNaBlokieProcedure;
import net.mcreator.borninchaosv.procedures.SweetSwordPriShchielchkiePKMProcedure;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * 为护符、霜噬之刃、甜食剑、召唤法杖、南瓜法杖等右键技能过程补全耐久扣减持有者与槽位上下文。
 */
@Mixin(value = {
        CharmofEnduranceproProcedure.class,
        CharmofFuryproProcedure.class,
        CharmofPowerPriShchielchkiePKMProcedure.class,
        CharmofResistanceproProcedure.class,
        CharmofStealthproProcedure.class,
        FrostbittenBladePriShchielchkiePKMProcedure.class,
        PumpkinStaffAtacProcedure.class,
        StaffoftheSummonerPriShchielchkiePravoiKnopkoiMyshiNaBlokieProcedure.class,
        SweetSwordPriShchielchkiePKMProcedure.class
}, remap = false)
public abstract class EquipmentDurabilityWorldEntityStackMixin {
    @Redirect(
            method = "execute(Lnet/minecraft/world/level/LevelAccessor;DDDLnet/minecraft/world/entity/Entity;Lnet/minecraft/world/item/ItemStack;)V",
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
            LevelAccessor world,
            double x,
            double y,
            double z,
            Entity entity,
            ItemStack itemstack
    ) {
        Batch5ProcedureHelper.hurtStackWithContext(stack, amount, level, originalPlayer, callback, entity);
    }
}
