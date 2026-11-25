package com.hp.borninchaosoptimizer.mixin.procedure;

import com.hp.borninchaosoptimizer.config.BicOptimizerConfig;
import net.mcreator.borninchaosv.procedures.DecrepitSkeletonPriObnovlieniiTaktaSushchnostiProcedure;
import net.mcreator.borninchaosv.procedures.RestlessSpiritPriObnovlieniiTaktaSushchnostiProcedure;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 优化十余种常见亡灵生物每 tick 双端无条件调用 canSeeSkyFromBelowWater 的性能热点：
 * 跳过客户端无效执行，先做夜晚/雨天/已着火 O(1) 短路，再按实体 ID 错峰检测天空遮挡。
 */
@Mixin(value = {
        DecrepitSkeletonPriObnovlieniiTaktaSushchnostiProcedure.class,
        RestlessSpiritPriObnovlieniiTaktaSushchnostiProcedure.class
}, remap = false)
public abstract class UndeadSunBurnProceduresMixin {
    @Inject(method = "execute", at = @At("HEAD"), cancellable = true)
    private static void bicOpt$optimizeSunCheck(LevelAccessor world, double x, double y, double z, Entity entity, CallbackInfo ci) {
        if (!BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.enableGlobalFixes)
                || !BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.optimizeSunAndSkyTickProcedures)) {
            return;
        }
        ci.cancel();
        if (entity == null || world.isClientSide() || !(world instanceof Level level)) {
            return;
        }
        if (entity.isOnFire() && entity.isInWaterRainOrBubble()) {
            if (world.canSeeSkyFromBelowWater(BlockPos.containing(x, y + 1.0, z))) {
                entity.clearFire();
            }
            return;
        }
        if (!level.isDay()
                || entity.isOnFire()
                || entity.isInWaterRainOrBubble()
                || level.getLevelData().isRaining()
                || level.getLevelData().isThundering()) {
            return;
        }
        int interval = Math.max(1, BicOptimizerConfig.getInt(BicOptimizerConfig.COMMON.sunCheckInterval));
        if ((entity.tickCount + entity.getId()) % interval != 0) {
            return;
        }
        if (world.canSeeSkyFromBelowWater(BlockPos.containing(x, y + 1.0, z))) {
            entity.setSecondsOnFire(5);
        }
    }
}
