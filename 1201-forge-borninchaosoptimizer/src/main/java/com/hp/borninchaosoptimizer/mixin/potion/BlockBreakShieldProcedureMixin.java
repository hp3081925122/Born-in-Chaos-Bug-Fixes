package com.hp.borninchaosoptimizer.mixin.potion;

import com.hp.borninchaosoptimizer.config.BicOptimizerConfig;
import com.hp.borninchaosoptimizer.util.BicCrossCheckHelper;
import net.mcreator.borninchaosv.procedures.BlockBreakKoghdaEffiektNachatprimienienProcedure;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 修复破防药水（Block Break）仅硬编码判断原版 Items.SHIELD 导致对模组盾牌完全无效，且未打断玩家当前举盾状态的缺陷。
 */
@Mixin(value = BlockBreakKoghdaEffiektNachatprimienienProcedure.class, remap = false)
public abstract class BlockBreakShieldProcedureMixin {
    @Inject(method = "execute", at = @At("HEAD"), cancellable = true)
    private static void bic$supportCustomShieldsAndStopBlocking(Entity entity, CallbackInfo ci) {
        if (!BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.enableGlobalFixes)
                || !BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.fixDurabilityAndShieldContext)) {
            return;
        }
        ci.cancel();
        if (!(entity instanceof Player player)) {
            return;
        }
        ItemStack offhand = player.getOffhandItem();
        ItemStack mainhand = player.getMainHandItem();
        boolean applied = false;
        if (BicCrossCheckHelper.isShield(offhand)) {
            player.getCooldowns().addCooldown(offhand.getItem(), 100);
            applied = true;
        } else if (BicCrossCheckHelper.isShield(mainhand)) {
            player.getCooldowns().addCooldown(mainhand.getItem(), 100);
            applied = true;
        }
        if (applied && player.isBlocking() && BicCrossCheckHelper.isShield(player.getUseItem())) {
            player.stopUsingItem();
        }
    }
}
