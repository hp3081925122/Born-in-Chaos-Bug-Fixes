package com.hp.borninchaosoptimizer.mixin;

import com.hp.borninchaosoptimizer.config.OptimizerConfig;
import net.mcreator.borninchaosv.procedures.DarkIceKoghdaSushchnostKhoditPoBlokuProcedure;
import net.mcreator.borninchaosv.procedures.FelSoilKoghdaSushchnostKhoditPoBlokuProcedure;
import net.mcreator.borninchaosv.procedures.FelsteedParProcedure;
import net.mcreator.borninchaosv.procedures.HeadlesPProcedure;
import net.mcreator.borninchaosv.procedures.LifestealerTicProcedure;
import net.mcreator.borninchaosv.procedures.LordPumpkinheadHeadPriObnovlieniiTikaSushchnostiProcedure;
import net.mcreator.borninchaosv.procedures.MaggotPriObnovlieniiTikaSushchnostiProcedure;
import net.mcreator.borninchaosv.procedures.MissionaryRaiderHitProcedure;
import net.mcreator.borninchaosv.procedures.MissionerPriObnovlieniiTikaSushchnostiProcedure;
import net.mcreator.borninchaosv.procedures.MissionerPriRanieniiSushchnostiProcedure;
import net.mcreator.borninchaosv.procedures.PumpkinheadPriObnovlieniiTaktaSushchnostiProcedure;
import net.mcreator.borninchaosv.procedures.ScarletpersecutorPriObnovlieniiTaktaSushchnostiProcedure;
import net.mcreator.borninchaosv.procedures.SearedSpiritPriObnovlieniiTaktaSushchnostiProcedure;
import net.mcreator.borninchaosv.procedures.SpiritofChaosticProcedure;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 拦截邪魇战马、无头骑士、南瓜头、窃命者真身、混沌之灵、猩红追猎者、传教士、焦灼之灵、蛆虫与暗冰/邪土过程在客户端侧的无效执行，
 * 消除客户端幽灵邪土/暗冰方块生成、客户端本地随机穿墙瞬移拉扯、客户端天气篡改与无效 hurt/removeEffect 事件风暴。
 */
@Mixin(value = {
        FelsteedParProcedure.class,
        HeadlesPProcedure.class,
        LordPumpkinheadHeadPriObnovlieniiTikaSushchnostiProcedure.class,
        PumpkinheadPriObnovlieniiTaktaSushchnostiProcedure.class,
        LifestealerTicProcedure.class,
        SpiritofChaosticProcedure.class,
        DarkIceKoghdaSushchnostKhoditPoBlokuProcedure.class,
        ScarletpersecutorPriObnovlieniiTaktaSushchnostiProcedure.class,
        MissionerPriObnovlieniiTikaSushchnostiProcedure.class,
        MissionaryRaiderHitProcedure.class,
        MissionerPriRanieniiSushchnostiProcedure.class,
        SearedSpiritPriObnovlieniiTaktaSushchnostiProcedure.class,
        FelSoilKoghdaSushchnostKhoditPoBlokuProcedure.class,
        MaggotPriObnovlieniiTikaSushchnostiProcedure.class
}, remap = false)
public abstract class ClientGhostMutationGuardMixin {
    @Inject(
            method = "execute(Lnet/minecraft/world/level/LevelAccessor;DDDLnet/minecraft/world/entity/Entity;)V",
            at = @At("HEAD"),
            cancellable = true,
            require = 0
    )
    private static void bic$serverOnlyWorldEntityTick(LevelAccessor world, double x, double y, double z, Entity entity, CallbackInfo ci) {
        if (OptimizerConfig.isEnabled(OptimizerConfig.FIX_CLIENT_SIDE_GHOST_MUTATIONS)
                && (entity == null || world.isClientSide() || entity.isRemoved())) {
            ci.cancel();
        }
    }

    @Inject(
            method = "execute(DDDLnet/minecraft/world/entity/Entity;)V",
            at = @At("HEAD"),
            cancellable = true,
            require = 0
    )
    private static void bic$serverOnlyUnstuckTeleport(double x, double y, double z, Entity entity, CallbackInfo ci) {
        if (OptimizerConfig.isEnabled(OptimizerConfig.FIX_CLIENT_SIDE_GHOST_MUTATIONS)
                && (entity == null || entity.level().isClientSide() || entity.isRemoved())) {
            ci.cancel();
        }
    }

    @Inject(
            method = "execute(Lnet/minecraft/world/level/LevelAccessor;Lnet/minecraft/world/entity/Entity;)V",
            at = @At("HEAD"),
            cancellable = true,
            require = 0
    )
    private static void bic$serverOnlyWorldEntityHurt(LevelAccessor world, Entity entity, CallbackInfo ci) {
        if (OptimizerConfig.isEnabled(OptimizerConfig.FIX_CLIENT_SIDE_GHOST_MUTATIONS)
                && (entity == null || world.isClientSide() || entity.isRemoved())) {
            ci.cancel();
        }
    }

    @Inject(
            method = "execute(Lnet/minecraft/world/entity/Entity;)V",
            at = @At("HEAD"),
            cancellable = true,
            require = 0
    )
    private static void bic$serverOnlyEntityStateTick(Entity entity, CallbackInfo ci) {
        if (OptimizerConfig.isEnabled(OptimizerConfig.FIX_CLIENT_SIDE_GHOST_MUTATIONS)
                && (entity == null || entity.level().isClientSide() || entity.isRemoved())) {
            ci.cancel();
        }
    }
}
