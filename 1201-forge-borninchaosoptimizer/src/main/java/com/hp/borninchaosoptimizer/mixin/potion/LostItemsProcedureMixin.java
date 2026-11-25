package com.hp.borninchaosoptimizer.mixin.potion;

import com.hp.borninchaosoptimizer.config.BicOptimizerConfig;
import net.mcreator.borninchaosv.init.BornInChaosV1ModMobEffects;
import net.mcreator.borninchaosv.procedures.LostItemsKazhdyiTikVoVriemiaEffiektaProcedure;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

/**
 * 替换失物寻找药水（Lost Items）每 tick 执行的完整 Brigadier 命令解析与全物品 NBT 序列化合并，
 * 改用原生 Java 球形半径地面掉落物扫描并直接设置发光标记（Spark 实测单玩家原占比高达 4.62%）。
 */
@Mixin(value = LostItemsKazhdyiTikVoVriemiaEffiektaProcedure.class, remap = false)
public abstract class LostItemsProcedureMixin {
    @Inject(method = "execute", at = @At("HEAD"), cancellable = true)
    private static void bicOptimizer$optimizeLostItems(LevelAccessor world, Entity entity, CallbackInfo ci) {
        if (!BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.enableGlobalFixes)
                || !BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.optimizeLostItemsEffect)) {
            return;
        }
        ci.cancel();
        if (entity == null || !(world instanceof ServerLevel serverLevel)) {
            return;
        }

        int interval = Math.max(1, BicOptimizerConfig.getInt(BicOptimizerConfig.COMMON.lostItemsScanInterval));
        if ((entity.tickCount + entity.getId()) % interval == 0) {
            double ex = entity.getX();
            double ey = entity.getY();
            double ez = entity.getZ();
            AABB searchBox = new AABB(ex - 10.5, ey - 10.5, ez - 10.5, ex + 10.5, ey + 10.5, ez + 10.5);
            List<ItemEntity> items = serverLevel.getEntitiesOfClass(
                    ItemEntity.class,
                    searchBox,
                    item -> item.onGround() && !item.hasGlowingTag() && item.distanceToSqr(ex, ey, ez) <= 100.0
            );
            for (ItemEntity item : items) {
                item.setGlowingTag(true);
            }
        }

        if (entity instanceof LivingEntity living) {
            MobEffect lostItems = BornInChaosV1ModMobEffects.LOST_ITEMS.get();
            MobEffectInstance instance = living.getEffect(lostItems);
            if (instance != null && instance.getDuration() < 20) {
                living.removeEffect(lostItems);
            }
        }
    }
}
