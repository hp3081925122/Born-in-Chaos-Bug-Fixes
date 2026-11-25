package com.hp.borninchaosoptimizer.mixin.seasonal;

import com.hp.borninchaosoptimizer.config.BicOptimizerConfig;
import com.hp.borninchaosoptimizer.util.SeasonalDateCache;
import net.mcreator.borninchaosv.init.BornInChaosV1ModGameRules;
import net.mcreator.borninchaosv.procedures.ChaoticSpringEventSpawnProcedure;
import net.mcreator.borninchaosv.procedures.ChillingHorrorEventSpawnProcedure;
import net.mcreator.borninchaosv.procedures.HalloweenEventSpawnProcedure;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 修复三个节日生成监听器的核心缺陷：
 * 1. 区块从磁盘重新加载实体（loadedFromDisk）时重复触发，导致拾取了玩家武器的僵尸/骷髅装备被覆盖吞没；
 * 2. 客户端与非节日时期对每个加入世界的实体重复执行多次 Calendar.getInstance() 时区与日历分配。
 */
@Mixin(value = {
        HalloweenEventSpawnProcedure.class,
        ChaoticSpringEventSpawnProcedure.class,
        ChillingHorrorEventSpawnProcedure.class
}, remap = false)
public abstract class SeasonalSpawnProceduresMixin {
    @Unique
    private static final String BIC_SEASONAL_CHECKED = "bic_opt_seasonal_checked";

    @Inject(method = "onEntitySpawned", at = @At("HEAD"), cancellable = true)
    private static void bic$guardSeasonalSpawn(EntityJoinLevelEvent event, CallbackInfo ci) {
        if (!BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.enableGlobalFixes)
                || !BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.fixSeasonalEvents)) {
            return;
        }
        if (event == null) {
            ci.cancel();
            return;
        }
        Level level = event.getLevel();
        Entity entity = event.getEntity();
        if (level == null || level.isClientSide() || event.loadedFromDisk() || entity == null) {
            ci.cancel();
            return;
        }
        if (!(entity instanceof Zombie || entity instanceof Skeleton)) {
            ci.cancel();
            return;
        }
        if (entity instanceof Mob mob && mob.isPersistenceRequired()) {
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
            return;
        }
        String checkKey = BIC_SEASONAL_CHECKED;
        int mask = entity.getPersistentData().getInt(checkKey);
        // 三个事件共用标记位：bit 0..2 由首次进服检查总次数控制，最多在新生成同一 tick 走完三个监听器一次
        if (mask >= 3) {
            ci.cancel();
            return;
        }
        entity.getPersistentData().putInt(checkKey, mask + 1);
    }
}
