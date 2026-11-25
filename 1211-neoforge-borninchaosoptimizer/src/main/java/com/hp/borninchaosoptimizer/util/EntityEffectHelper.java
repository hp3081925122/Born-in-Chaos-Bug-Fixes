package com.hp.borninchaosoptimizer.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;

/**
 * 生物每刻被动药水刷新、安全移除与阳光照射短路判定辅助工具。
 */
public final class EntityEffectHelper {
    private EntityEffectHelper() {
    }

    /**
     * 仅在目标身上缺少对应药水、等级不足或剩余时长低于阈值时才重新施加效果，
     * 避免每刻调用 addEffect 触发属性修饰符重算与全服效果同步发包风暴。
     */
    public static void refreshPassiveEffect(LivingEntity entity, Holder<MobEffect> effect, int duration, int amplifier, int minRemainingTicks) {
        if (entity.level().isClientSide()) {
            return;
        }
        MobEffectInstance current = entity.getEffect(effect);
        if (current == null || current.getAmplifier() < amplifier || current.getDuration() <= minRemainingTicks) {
            entity.addEffect(new MobEffectInstance(effect, duration, amplifier, false, false));
        }
    }

    /**
     * 仅在实体确实持有目标效果时才调用 removeEffect，避免每刻向事件总线无意义广播 MobEffectEvent.Remove。
     */
    public static void removeEffectIfPresent(LivingEntity entity, Holder<MobEffect> effect) {
        if (entity.hasEffect(effect)) {
            entity.removeEffect(effect);
        }
    }

    /**
     * 先在常数时间内检查服务端、白天、晴天与火焰状态，最后才执行昂贵的 canSeeSkyFromBelowWater 方块柱扫描，
     * 同时完整保留原有的遇水/淋雨灭火逻辑。
     */
    public static void handleUndeadSunburn(LevelAccessor world, double x, double y, double z, Entity entity) {
        if (entity.isInWaterRainOrBubble()) {
            if (entity.isOnFire()) {
                entity.clearFire();
            }
            return;
        }
        if (world.isClientSide() || entity.isOnFire() || !(world instanceof Level level)) {
            return;
        }
        if (!level.isDay() || level.isRaining() || level.isThundering()) {
            return;
        }
        if (world.canSeeSkyFromBelowWater(BlockPos.containing(x, y + 1.0, z))) {
            entity.igniteForSeconds(5.0f);
        }
    }
}
