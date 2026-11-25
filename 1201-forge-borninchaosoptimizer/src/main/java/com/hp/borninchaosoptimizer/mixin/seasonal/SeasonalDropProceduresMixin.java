package com.hp.borninchaosoptimizer.mixin.seasonal;

import com.hp.borninchaosoptimizer.config.BicOptimizerConfig;
import com.hp.borninchaosoptimizer.util.SeasonalDateCache;
import net.mcreator.borninchaosv.init.BornInChaosV1ModGameRules;
import net.mcreator.borninchaosv.procedures.ChaoticSpringEventDropProcedure;
import net.mcreator.borninchaosv.procedures.ChillingHorrorEventDropProcedure;
import net.mcreator.borninchaosv.procedures.HalloweenEventDropProcedure;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 在非节日时期与客户端短路三个节日掉落监听器，消除每次生物死亡多达 15 次的 Calendar.getInstance() 分配。
 */
@Mixin(value = {
        HalloweenEventDropProcedure.class,
        ChaoticSpringEventDropProcedure.class,
        ChillingHorrorEventDropProcedure.class
}, remap = false)
public abstract class SeasonalDropProceduresMixin {
    @Inject(method = "onEntityDeath", at = @At("HEAD"), cancellable = true)
    private static void bic$fastSeasonalDropFilter(LivingDeathEvent event, CallbackInfo ci) {
        if (!BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.enableGlobalFixes)
                || !BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.fixSeasonalEvents)) {
            return;
        }
        if (event == null) {
            ci.cancel();
            return;
        }
        LivingEntity entity = event.getEntity();
        if (entity == null || !(entity instanceof Zombie || entity instanceof Skeleton)) {
            ci.cancel();
            return;
        }
        Level level = entity.level();
        if (level.isClientSide()) {
            ci.cancel();
            return;
        }
        GameRules rules = level.getLevelData().getGameRules();
        if (!rules.getBoolean(BornInChaosV1ModGameRules.SEASONAL_EVENTS)) {
            ci.cancel();
            return;
        }
        boolean anyActive = SeasonalDateCache.isHalloweenWindow()
                || SeasonalDateCache.isChaoticSpringWindow()
                || SeasonalDateCache.isChillingHorrorWindow()
                || rules.getBoolean(BornInChaosV1ModGameRules.HALLOWEEN_EVENT)
                || rules.getBoolean(BornInChaosV1ModGameRules.CHAOTIC_SPRING_EVENT)
                || rules.getBoolean(BornInChaosV1ModGameRules.CHILLING_HORROR_EVENT);
        if (!anyActive) {
            ci.cancel();
        }
    }
}
