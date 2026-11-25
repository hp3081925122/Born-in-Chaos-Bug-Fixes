package com.hp.borninchaosoptimizer.mixin.procedure;

import com.hp.borninchaosoptimizer.config.BicOptimizerConfig;
import net.mcreator.borninchaosv.procedures.MissionaryRaiderHitProcedure;
import net.mcreator.borninchaosv.procedures.MissionerPriRanieniiSushchnostiProcedure;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 限制传教士与袭击者传教士受击瞬移过程仅在服务端执行，
 * 阻止客户端篡改客户端世界天气、实体 PersistentData 与双端异步瞬移。
 */
@Mixin(value = {
        MissionerPriRanieniiSushchnostiProcedure.class,
        MissionaryRaiderHitProcedure.class
}, remap = false)
public abstract class MissionaryHitServerGuardMixin {
    @Inject(method = "execute", at = @At("HEAD"), cancellable = true)
    private static void bicOpt$serverOnly(LevelAccessor world, double x, double y, double z, Entity entity, CallbackInfo ci) {
        if (BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.enableGlobalFixes)
                && BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.fixBossTransformAndTeleportBugs)
                && (entity == null || world.isClientSide())) {
            ci.cancel();
        }
    }
}
