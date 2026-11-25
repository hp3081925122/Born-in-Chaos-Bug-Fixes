package com.hp.borninchaosoptimizer.mixin.procedure;

import com.hp.borninchaosoptimizer.config.BicOptimizerConfig;
import net.mcreator.borninchaosv.entity.ControlledBabySkeletonEntity;
import net.mcreator.borninchaosv.entity.ControlledSpiritualAssistantEntity;
import net.mcreator.borninchaosv.procedures.BonescallerStaffKazhdyiTikVInvientarieProcedure;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

/**
 * 优化唤骨法杖背包每 tick 过程：仅服务端按间隔执行，并改用精确 TamableAnimal 类型过滤替代全实体流式排序。
 */
@Mixin(value = BonescallerStaffKazhdyiTikVInvientarieProcedure.class, remap = false)
public abstract class BonescallerStaffTickMixin {
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
        AABB box = new AABB(x - 5.0, y - 5.0, z - 5.0, x + 5.0, y + 5.0, z + 5.0);
        List<TamableAnimal> minions = world.getEntitiesOfClass(
                TamableAnimal.class,
                box,
                m -> !m.isTame() && (m instanceof ControlledBabySkeletonEntity || m instanceof ControlledSpiritualAssistantEntity));
        for (TamableAnimal minion : minions) {
            minion.tame(player);
        }
    }
}
