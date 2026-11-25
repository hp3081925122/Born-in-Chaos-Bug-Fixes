package com.hp.borninchaosoptimizer.mixin.procedure;

import com.hp.borninchaosoptimizer.config.BicOptimizerConfig;
import com.hp.borninchaosoptimizer.util.SeasonalDateCache;
import net.mcreator.borninchaosv.init.BornInChaosV1ModItems;
import net.mcreator.borninchaosv.procedures.NightmareStalkerPriGibieliSushchnostiProcedure;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 优化梦魇潜行者死亡过程：替换三次 Calendar.getInstance() 为缓存日期窗口，并缓存进度 ResourceLocation。
 */
@Mixin(value = NightmareStalkerPriGibieliSushchnostiProcedure.class, remap = false)
public abstract class NightmareStalkerDeathProcedureMixin {
    @Unique
    private static final ResourceLocation BIC_OPT$FIGHTING_NIGHTMARES_ADV =
            ResourceLocation.tryBuild("born_in_chaos_v1", "fighting_nightmares");

    @Inject(method = "execute", at = @At("HEAD"), cancellable = true)
    private static void bicOpt$optimizeDeath(LevelAccessor world, Entity entity, Entity sourceentity, CallbackInfo ci) {
        if (!BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.enableGlobalFixes)
                || !BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.optimizeSunAndSkyTickProcedures)) {
            return;
        }
        ci.cancel();
        if (entity == null || sourceentity == null || world.isClientSide()) {
            return;
        }
        if (sourceentity instanceof ServerPlayer serverPlayer && BIC_OPT$FIGHTING_NIGHTMARES_ADV != null) {
            Advancement adv = serverPlayer.server.getAdvancements().getAdvancement(BIC_OPT$FIGHTING_NIGHTMARES_ADV);
            if (adv != null) {
                AdvancementProgress progress = serverPlayer.getAdvancements().getOrStartProgress(adv);
                if (!progress.isDone()) {
                    for (String criterion : progress.getRemainingCriteria()) {
                        serverPlayer.getAdvancements().award(adv, criterion);
                    }
                }
            }
        }
        if (world instanceof ServerLevel serverLevel && SeasonalDateCache.isChocolateHeartDropWindow()) {
            int count = Mth.nextInt(serverLevel.getRandom(), 1, 3);
            for (int i = 0; i < count; i++) {
                ItemEntity drop = new ItemEntity(
                        serverLevel,
                        entity.getX(),
                        entity.getY() + 1.0,
                        entity.getZ(),
                        new ItemStack(BornInChaosV1ModItems.CHOCOLATE_HEART.get()));
                drop.setPickUpDelay(10);
                serverLevel.addFreshEntity(drop);
            }
        }
    }
}
