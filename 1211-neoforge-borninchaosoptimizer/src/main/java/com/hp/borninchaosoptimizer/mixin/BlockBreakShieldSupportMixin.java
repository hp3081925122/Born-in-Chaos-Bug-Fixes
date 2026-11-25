package com.hp.borninchaosoptimizer.mixin;

import com.hp.borninchaosoptimizer.config.OptimizerConfig;
import net.mcreator.borninchaosv.procedures.BlockBreakKoghdaEffiektNachatprimienienProcedure;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import net.neoforged.neoforge.common.ItemAbilities;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 修复破防（Block Break）效果开始时仅硬编码识别原版 Items.SHIELD 而忽略其他模组盾牌的问题，并在进入冷却时主动打断举盾姿态。
 */
@Mixin(value = BlockBreakKoghdaEffiektNachatprimienienProcedure.class, remap = false)
public abstract class BlockBreakShieldSupportMixin {
    @Inject(method = "execute", at = @At("HEAD"), cancellable = true)
    private static void bic$supportAllShieldItems(Entity entity, CallbackInfo ci) {
        if (!OptimizerConfig.isEnabled(OptimizerConfig.FIX_EQUIPMENT_DURABILITY_CONTEXT)) {
            return;
        }
        ci.cancel();
        if (!(entity instanceof Player player)) {
            return;
        }
        ItemStack offhand = player.getOffhandItem();
        ItemStack mainhand = player.getMainHandItem();
        if (offhand.getItem() instanceof ShieldItem || offhand.canPerformAction(ItemAbilities.SHIELD_BLOCK)) {
            player.getCooldowns().addCooldown(offhand.getItem(), 100);
            if (player.isBlocking()) {
                player.stopUsingItem();
            }
        } else if (mainhand.getItem() instanceof ShieldItem || mainhand.canPerformAction(ItemAbilities.SHIELD_BLOCK)) {
            player.getCooldowns().addCooldown(mainhand.getItem(), 100);
            if (player.isBlocking()) {
                player.stopUsingItem();
            }
        }
    }
}
