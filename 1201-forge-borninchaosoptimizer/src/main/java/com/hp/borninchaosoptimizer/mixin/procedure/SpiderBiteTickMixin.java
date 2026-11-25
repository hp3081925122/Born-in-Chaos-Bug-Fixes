package com.hp.borninchaosoptimizer.mixin.procedure;

import com.hp.borninchaosoptimizer.config.BicOptimizerConfig;
import net.mcreator.borninchaosv.entity.BabySpiderControlledEntity;
import net.mcreator.borninchaosv.procedures.SpiderBiteKazhdyiTikVInvientarieProcedure;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

/**
 * 优化蛛咬法杖背包每 tick 过程：仅服务端按间隔执行，并直接查询受控幼蛛类型。
 */
@Mixin(value = SpiderBiteKazhdyiTikVInvientarieProcedure.class, remap = false)
public abstract class SpiderBiteTickMixin {
    @Inject(method = "execute", at = @At("HEAD"), cancellable = true)
    private static void bicOpt$optimizeTameScan(LevelAccessor world, double x, double y, double z, Entity entity, CallbackInfo ci) {
        if (!BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.enableGlobalFixes)
                || !BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.optimizeInventoryAndArmorTicks)) {
            return;
        }
        ci.cancel();
        if (world.isClientSide() || !(entity instanceof Player player)) {
            return;
        }
        int interval = Math.max(1, BicOptimizerConfig.getInt(BicOptimizerConfig.COMMON.minionTameScanInterval));
        if ((player.tickCount + player.getId()) % interval != 0) {
            return;
        }
        AABB box = new AABB(x - 3.0, y - 3.0, z - 3.0, x + 3.0, y + 3.0, z + 3.0);
        List<BabySpiderControlledEntity> minions = world.getEntitiesOfClass(
                BabySpiderControlledEntity.class, box, m -> !m.isTame());
        for (BabySpiderControlledEntity minion : minions) {
            minion.tame(player);
        }
    }
}
