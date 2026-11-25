package com.hp.borninchaosoptimizer.mixin;

import com.hp.borninchaosoptimizer.config.OptimizerConfig;
import com.hp.borninchaosoptimizer.util.Batch5ProcedureHelper;
import net.mcreator.borninchaosv.procedures.LordPumpkinheadHeadhitProcedure;
import net.mcreator.borninchaosv.procedures.LordPumpkinheadPriRanieniiSushchnostiProcedure;
import net.mcreator.borninchaosv.procedures.LordPumpkinheadhitProcedure;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * 修复南瓜领主（骑马、无马、头颅三形态）受击瞬移缺陷：
 * 1. 原代码在实体浮点坐标上重复叠加 +0.5 导致落点位于方块棱边，半边碰撞箱极易嵌进相邻墙体窒息；
 * 2. -Z 兜底分支无遮挡校验，且全部分支均漏判岩浆流体（canOcclude 为 false）；
 * 3. 对齐方块中心并按实体真实碰撞箱与下方岩浆做安全校验，四个正交方向均不安全时保持原地。
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
        double alignedTargetX = Mth.floor(targetX) + 0.5;
        double alignedTargetZ = Mth.floor(targetZ) + 0.5;
        if (Batch5ProcedureHelper.isSafeEntityTeleportSpot(level, entity, alignedTargetX, targetY, alignedTargetZ, false)) {
            entity.teleportTo(alignedTargetX, targetY, alignedTargetZ);
            return;
        }
        double baseX = Mth.floor(entity.getX()) + 0.5;
        double baseZ = Mth.floor(entity.getZ()) + 0.5;
        int dist = Math.max((int) Math.round(Math.max(Math.abs(targetX - entity.getX()), Math.abs(targetZ - entity.getZ()))), 10);
        double[][] fallbacks = {
                {baseX + dist, targetY, baseZ},
                {baseX - dist, targetY, baseZ},
                {baseX, targetY, baseZ + dist},
                {baseX, targetY, baseZ - dist}
        };
        for (double[] candidate : fallbacks) {
            if (Batch5ProcedureHelper.isSafeEntityTeleportSpot(level, entity, candidate[0], candidate[1], candidate[2], false)) {
                entity.teleportTo(candidate[0], candidate[1], candidate[2]);
                return;
            }
        }
    }
}
