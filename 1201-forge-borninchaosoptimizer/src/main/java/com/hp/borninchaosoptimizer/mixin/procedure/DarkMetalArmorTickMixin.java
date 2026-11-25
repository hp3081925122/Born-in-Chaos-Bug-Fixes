package com.hp.borninchaosoptimizer.mixin.procedure;

import com.hp.borninchaosoptimizer.config.BicOptimizerConfig;
import net.mcreator.borninchaosv.init.BornInChaosV1ModItems;
import net.mcreator.borninchaosv.init.BornInChaosV1ModMobEffects;
import net.mcreator.borninchaosv.procedures.DarkMetalArmorSobytiieTaktovShliemaProcedure;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 优化暗金属套装每 tick 过程：
 * 1. 仅服务端执行，并在同一游戏刻去重（原模组头胸腿脚 4 部件每 tick 双端重复调用 8 次）；
 * 2. 先检查 hasEffect 再移除凋零，停止每秒 160 次无条件 MobEffectEvent.Remove 事件风暴；
 * 3. 缓存 chaos_knight 进度 ResourceLocation 并降频查询进度表。
 */
@Mixin(value = DarkMetalArmorSobytiieTaktovShliemaProcedure.class, remap = false)
public abstract class DarkMetalArmorTickMixin {
    @Unique
    private static final String BIC_OPT$LAST_TICK_KEY = "bic_opt_dm_armor_tick";
    @Unique
    private static final ResourceLocation BIC_OPT$CHAOS_KNIGHT_ADV =
            ResourceLocation.tryBuild("born_in_chaos_v1", "chaos_knight");

    @Inject(method = "execute", at = @At("HEAD"), cancellable = true)
    private static void bicOpt$optimizeArmorTick(Entity entity, CallbackInfo ci) {
        if (!BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.enableGlobalFixes)
                || !BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.optimizeInventoryAndArmorTicks)) {
            return;
        }
        ci.cancel();
        if (!(entity instanceof LivingEntity living) || living.level().isClientSide()) {
            return;
        }

        long gameTime = living.level().getGameTime();
        if (entity.getPersistentData().getLong(BIC_OPT$LAST_TICK_KEY) == gameTime) {
            return;
        }
        entity.getPersistentData().putLong(BIC_OPT$LAST_TICK_KEY, gameTime);

        if (living.getItemBySlot(EquipmentSlot.HEAD).getItem() != BornInChaosV1ModItems.DARK_METAL_ARMOR_HELMET.get()
                || living.getItemBySlot(EquipmentSlot.LEGS).getItem() != BornInChaosV1ModItems.DARK_METAL_ARMOR_LEGGINGS.get()
                || living.getItemBySlot(EquipmentSlot.CHEST).getItem() != BornInChaosV1ModItems.DARK_METAL_ARMOR_CHESTPLATE.get()
                || living.getItemBySlot(EquipmentSlot.FEET).getItem() != BornInChaosV1ModItems.DARK_METAL_ARMOR_BOOTS.get()) {
            return;
        }

        if (living.hasEffect(MobEffects.WITHER)) {
            living.removeEffect(MobEffects.WITHER);
        }
        if (living.getHealth() <= 6.0f) {
            living.addEffect(new MobEffectInstance(BornInChaosV1ModMobEffects.RAMPANT_RAMPAGE.get(), 100, 0));
            living.addEffect(new MobEffectInstance(MobEffects.HUNGER, 100, 2, false, false));
        }

        if (entity instanceof ServerPlayer serverPlayer
                && BIC_OPT$CHAOS_KNIGHT_ADV != null
                && (serverPlayer.tickCount + serverPlayer.getId()) % 20 == 0) {
            Advancement adv = serverPlayer.server.getAdvancements().getAdvancement(BIC_OPT$CHAOS_KNIGHT_ADV);
            if (adv != null) {
                AdvancementProgress progress = serverPlayer.getAdvancements().getOrStartProgress(adv);
                if (!progress.isDone()) {
                    for (String criterion : progress.getRemainingCriteria()) {
                        serverPlayer.getAdvancements().award(adv, criterion);
                    }
                }
            }
        }
    }
}
