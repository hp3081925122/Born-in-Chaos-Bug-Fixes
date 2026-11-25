package com.hp.borninchaosoptimizer.mixin;

import com.hp.borninchaosoptimizer.config.OptimizerConfig;
import net.mcreator.borninchaosv.procedures.CorpseFishPriProcedure;
import net.mcreator.borninchaosv.procedures.GluttonFishPriProcedure;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.LevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 修复腐尸鱼与暴食鱼作为 WATER_CREATURE 分类敌对怪绕过原版和平模式过滤，
 * 在和平难度下反复刷出并立刻被 Monster.checkDespawn 清除的空转缺陷。
 */
@Mixin(value = {
        CorpseFishPriProcedure.class,
        GluttonFishPriProcedure.class
}, remap = false)
public abstract class FishPeacefulSpawnFixMixin {
    @Inject(method = "execute", at = @At("HEAD"), cancellable = true)
    private static void bic$denyHostileFishInPeaceful(
            LevelAccessor world, double x, double y, double z, CallbackInfoReturnable<Boolean> cir
    ) {
        if (!OptimizerConfig.isEnabled(OptimizerConfig.FIX_SPAWN_CONDITIONS)) {
            return;
        }
        if (world != null && world.getDifficulty() == Difficulty.PEACEFUL) {
            cir.setReturnValue(false);
        }
    }
}
