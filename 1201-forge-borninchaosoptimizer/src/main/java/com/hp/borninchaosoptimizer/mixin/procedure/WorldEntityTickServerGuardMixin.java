package com.hp.borninchaosoptimizer.mixin.procedure;

import com.hp.borninchaosoptimizer.config.BicOptimizerConfig;
import net.mcreator.borninchaosv.procedures.FelsteedParProcedure;
import net.mcreator.borninchaosv.procedures.HeadlesPProcedure;
import net.mcreator.borninchaosv.procedures.LifestealerTicProcedure;
import net.mcreator.borninchaosv.procedures.LordPumpkinheadHeadPriObnovlieniiTikaSushchnostiProcedure;
import net.mcreator.borninchaosv.procedures.PumpkinheadPriObnovlieniiTaktaSushchnostiProcedure;
import net.mcreator.borninchaosv.procedures.SpiritofChaosticProcedure;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 拦截邪魇战马、无头骑士、南瓜头、窃命者真身与混沌之灵每 tick 过程在客户端的无效执行：
 * 消除客户端幽灵邪土/暗冰方块、客户端独立随机卡墙瞬移及客户端 PersistentData 竞争篡改，
 * 并阻止已移除实体在同一 tick 重复触发混沌之灵爆炸。
 */
@Mixin(value = {
        FelsteedParProcedure.class,
        HeadlesPProcedure.class,
        LordPumpkinheadHeadPriObnovlieniiTikaSushchnostiProcedure.class,
        PumpkinheadPriObnovlieniiTaktaSushchnostiProcedure.class,
        LifestealerTicProcedure.class,
        SpiritofChaosticProcedure.class
}, remap = false)
public abstract class WorldEntityTickServerGuardMixin {
    @Inject(method = "execute", at = @At("HEAD"), cancellable = true)
    private static void bicOpt$serverOnlyAndAlive(LevelAccessor world, double x, double y, double z, Entity entity, CallbackInfo ci) {
        if (BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.enableGlobalFixes)
                && BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.fixEntityTickClientSideMutations)
                && (entity == null || world.isClientSide() || entity.isRemoved())) {
            ci.cancel();
        }
    }
}
