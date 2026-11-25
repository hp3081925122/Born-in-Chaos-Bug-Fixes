package com.hp.borninchaosoptimizer.mixin;

import com.hp.borninchaosoptimizer.config.OptimizerConfig;
import net.mcreator.borninchaosv.procedures.LordPumpkinheadHeadhitProcedure;
import net.mcreator.borninchaosv.procedures.LordPumpkinheadPriRanieniiSushchnostiProcedure;
import net.mcreator.borninchaosv.procedures.LordPumpkinheadhitProcedure;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * 修复南瓜领主（无马形态、头颅形态）与南瓜爵士受击瞬移的三大缺陷：
 * 1. -X 分支原代码误拿 +X 坐标判断积雪方块导致瞬移进实心方块；
 * 2. -Z 兜底 else 分支完全缺失方块遮挡检查，导致 100% 瞬移进实心墙体/岩层窒息；
 * 3. 若选定目标点被阻挡，自动依次尝试其余三个正交方向的安全落点，四个方向均被墙体封死时保持原地不卡墙。
 */
@Mixin(value = {
        LordPumpkinheadPriRanieniiSushchnostiProcedure.class,
        LordPumpkinheadhitProcedure.class,
        LordPumpkinheadHeadhitProcedure.class
}, remap = false)
public abstract class LordPumpkinheadTeleportFixMixin {
    @Redirect(
            method = "execute",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/Entity;teleportTo(DDD)V"
            )
    )
    private static void bic$safeBossTeleport(Entity entity, double targetX, double targetY, double targetZ) {
        if (!OptimizerConfig.isEnabled(OptimizerConfig.FIX_BOSS_TELEPORT_INTO_WALLS)) {
            entity.teleportTo(targetX, targetY, targetZ);
            return;
        }
        Level level = entity.level();
        if (level.isClientSide()) {
            return;
        }
        if (bic$isSafeDestination(level, targetX, targetY, targetZ)) {
            entity.teleportTo(targetX, targetY, targetZ);
            return;
        }
        double originX = entity.getX();
        double originZ = entity.getZ();
        double dist = Math.max(Math.abs(targetX - originX), Math.abs(targetZ - originZ));
        if (dist < 4.0) {
            dist = 10.0;
        }
        double[][] fallbacks = {
                {originX + dist, targetY, originZ + 0.5},
                {originX - dist, targetY, originZ + 0.5},
                {originX + 0.5, targetY, originZ + dist},
                {originX + 0.5, targetY, originZ - dist}
        };
        for (double[] candidate : fallbacks) {
            if (bic$isSafeDestination(level, candidate[0], candidate[1], candidate[2])) {
                entity.teleportTo(candidate[0], candidate[1], candidate[2]);
                return;
            }
        }
    }

    @Unique
    private static boolean bic$isSafeDestination(Level level, double tx, double ty, double tz) {
        BlockPos feetPos = BlockPos.containing(tx, ty, tz);
        if (!level.hasChunkAt(feetPos)) {
            return false;
        }
        BlockPos headPos = BlockPos.containing(tx, ty + 1.0, tz);
        if (level.getBlockState(headPos).canOcclude()) {
            return false;
        }
        BlockState feetState = level.getBlockState(feetPos);
        return !feetState.canOcclude() || feetState.is(Blocks.SNOW);
    }
}
