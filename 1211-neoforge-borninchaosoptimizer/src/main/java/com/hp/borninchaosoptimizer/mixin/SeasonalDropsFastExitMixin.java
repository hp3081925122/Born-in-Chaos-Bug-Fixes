package com.hp.borninchaosoptimizer.mixin;

import com.hp.borninchaosoptimizer.config.OptimizerConfig;
import com.hp.borninchaosoptimizer.util.SeasonalDateCache;
import net.mcreator.borninchaosv.init.BornInChaosV1ModGameRules;
import net.mcreator.borninchaosv.procedures.ChaoticSpringEventDropProcedure;
import net.mcreator.borninchaosv.procedures.ChillingHorrorEventDropProcedure;
import net.mcreator.borninchaosv.procedures.HalloweenEventDropProcedure;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.LevelAccessor;
import net.neoforged.bus.api.Event;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 为三个季节掉落过程提供日期缓存短路检查，消除非活动季节每次僵尸/骷髅死亡时重复实例化多个 Calendar 的开销。
 */
@Mixin(value = {
        ChaoticSpringEventDropProcedure.class,
        ChillingHorrorEventDropProcedure.class,
        HalloweenEventDropProcedure.class
}, remap = false)
public abstract class SeasonalDropsFastExitMixin {
    @Inject(method = "execute(Lnet/neoforged/bus/api/Event;Lnet/minecraft/world/level/LevelAccessor;DDDLnet/minecraft/world/entity/Entity;)V", at = @At("HEAD"), cancellable = true)
    private static void bic$fastExitOutOfSeason(Event event, LevelAccessor world, double x, double y, double z, Entity entity, CallbackInfo ci) {
        if (!OptimizerConfig.isEnabled(OptimizerConfig.FIX_SEASONAL_RELOAD_OVERWRITE)) {
            return;
        }
        if (world.isClientSide() || !(entity instanceof Zombie || entity instanceof Skeleton)) {
            ci.cancel();
            return;
        }
        GameRules rules = world.getLevelData().getGameRules();
        if (!rules.getBoolean(BornInChaosV1ModGameRules.SEASONAL_EVENTS)) {
            ci.cancel();
            return;
        }
        boolean anyActive = SeasonalDateCache.isChaoticSpringDate()
                || SeasonalDateCache.isChillingHorrorDate()
                || SeasonalDateCache.isHalloweenDate()
                || rules.getBoolean(BornInChaosV1ModGameRules.CHAOTIC_SPRING_EVENT)
                || rules.getBoolean(BornInChaosV1ModGameRules.CHILLING_HORROR_EVENT)
                || rules.getBoolean(BornInChaosV1ModGameRules.HALLOWEEN_EVENT);
        if (!anyActive) {
            ci.cancel();
        }
    }
}
