package com.hp.borninchaosoptimizer.mixin.procedure;

import com.hp.borninchaosoptimizer.config.BicOptimizerConfig;
import net.mcreator.borninchaosv.procedures.DarkMetalDepositKoghdaBlokRazrushienIghrokomProcedure;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 修复暗金属矿脉破坏过程实例化含客户端 Minecraft 引用的匿名内部类及客户端无效空跑问题。
 */
@Mixin(value = DarkMetalDepositKoghdaBlokRazrushienIghrokomProcedure.class, remap = false)
public abstract class DarkMetalDepositBreakProcedureMixin {
    @Inject(method = "execute", at = @At("HEAD"), cancellable = true)
    private static void bicOpt$safeExecute(LevelAccessor world, double x, double y, double z, Entity entity, CallbackInfo ci) {
        if (!BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.enableGlobalFixes)
                || !BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.fixDedicatedServerClientRefs)) {
            return;
        }
        ci.cancel();
        if (world.isClientSide() || !(world instanceof ServerLevel serverLevel)) {
            return;
        }
        if (!(entity instanceof ServerPlayer serverPlayer)
                || serverPlayer.gameMode.getGameModeForPlayer() != GameType.SURVIVAL) {
            return;
        }
        ItemStack mainHand = serverPlayer.getMainHandItem();
        if (mainHand.getEnchantmentLevel(Enchantments.SILK_TOUCH) == 0) {
            int count = Mth.nextInt(serverLevel.getRandom(), 2, 4);
            for (int i = 0; i < count; i++) {
                serverLevel.addFreshEntity(new ExperienceOrb(serverLevel, x, y, z, 2));
            }
        }
    }
}
