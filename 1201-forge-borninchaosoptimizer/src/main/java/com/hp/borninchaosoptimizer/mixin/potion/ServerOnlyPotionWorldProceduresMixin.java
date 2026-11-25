package com.hp.borninchaosoptimizer.mixin.potion;

import com.hp.borninchaosoptimizer.config.BicOptimizerConfig;
import net.mcreator.borninchaosv.procedures.DetonationKazhdyiTikVoVriemiaEffiektaProcedure;
import net.mcreator.borninchaosv.procedures.LifestealKazhdyiTikVoVriemiaEffiektaProcedure;
import net.mcreator.borninchaosv.procedures.MinionSummonKazhdyiTikVoVriemiaEffiektaProcedure;
import net.mcreator.borninchaosv.procedures.SoulStratificationKazhdyiTikVoVriemiaEffiektaProcedure;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 拦截吸血结算、爆破、仆从召唤与灵魂剥离在客户端侧的误执行，避免客户端假伤、提前删状态与重复本地音效。
 */
@Mixin(value = {
        LifestealKazhdyiTikVoVriemiaEffiektaProcedure.class,
        DetonationKazhdyiTikVoVriemiaEffiektaProcedure.class,
        MinionSummonKazhdyiTikVoVriemiaEffiektaProcedure.class,
        SoulStratificationKazhdyiTikVoVriemiaEffiektaProcedure.class
}, remap = false)
public abstract class ServerOnlyPotionWorldProceduresMixin {
    @Inject(method = "execute", at = @At("HEAD"), cancellable = true)
    private static void bicOptimizer$skipOnClient(LevelAccessor world, double x, double y, double z, Entity entity, CallbackInfo ci) {
        if (world == null || !world.isClientSide()) {
            return;
        }
        if (!BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.enableGlobalFixes)
                || !BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.optimizePotionAuraAndStateTicks)) {
            return;
        }
        ci.cancel();
    }
}
