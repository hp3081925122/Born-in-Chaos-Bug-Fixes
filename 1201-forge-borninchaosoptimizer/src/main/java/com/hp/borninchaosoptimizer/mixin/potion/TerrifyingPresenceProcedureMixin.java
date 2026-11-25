package com.hp.borninchaosoptimizer.mixin.potion;

import com.hp.borninchaosoptimizer.config.BicOptimizerConfig;
import net.mcreator.borninchaosv.init.BornInChaosV1ModMobEffects;
import net.mcreator.borninchaosv.init.BornInChaosV1ModParticleTypes;
import net.mcreator.borninchaosv.procedures.TerrifyingPresenceKazhdyiTikVoVriemiaEffiektaProcedure;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

/**
 * 优化梦魇潜行者恐惧威压（Terrifying Presence）：
 * 保留双端 setMaxUpStep(3.0f)，将状态修改限制在服务端，消除无条件 removeEffect 事件广播，
 * 并将原本对全 Entity.class 的排序流查询精确缩小为仅扫描 Player.class。
 */
@Mixin(value = TerrifyingPresenceKazhdyiTikVoVriemiaEffiektaProcedure.class, remap = false)
public abstract class TerrifyingPresenceProcedureMixin {
    @Inject(method = "execute", at = @At("HEAD"), cancellable = true)
    private static void bicOptimizer$optimizeTerrifyingPresence(LevelAccessor world, double x, double y, double z, Entity entity, CallbackInfo ci) {
        if (!BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.enableGlobalFixes)
                || !BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.optimizePotionAuraAndStateTicks)) {
            return;
        }
        ci.cancel();
        if (entity == null) {
            return;
        }

        entity.setMaxUpStep(3.0f);
        if (!(world instanceof ServerLevel serverLevel)) {
            return;
        }

        if (entity instanceof LivingEntity living) {
            if (!living.isAlive()) {
                MobEffect terrifying = BornInChaosV1ModMobEffects.TERRIFYING_PRESENCE.get();
                if (living.hasEffect(terrifying)) {
                    living.removeEffect(terrifying);
                }
            }
            if (living.hasEffect(MobEffects.DAMAGE_BOOST)) {
                living.removeEffect(MobEffects.DAMAGE_BOOST);
            }
            living.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 10, 2, false, false));
        }

        double ex = entity.getX();
        double ey = entity.getY();
        double ez = entity.getZ();
        serverLevel.sendParticles(BornInChaosV1ModParticleTypes.DARKMATTER.get(), ex, ey + 2.0, ez, 1, 0.3, 0.3, 0.3, 0.3);
        serverLevel.sendParticles(BornInChaosV1ModParticleTypes.DARKSPOTS.get(), ex, ey + 0.3, ez, 3, 2.5, 0.1, 2.5, 0.2);

        double timer = entity.getPersistentData().getDouble("terrifyingpresence");
        if (timer <= 0.0) {
            timer = 30.0;
        } else {
            timer -= 1.0;
        }
        entity.getPersistentData().putDouble("terrifyingpresence", timer);

        if (timer != 0.0) {
            return;
        }

        if (entity instanceof LivingEntity living && living.getHealth() > 10.0f) {
            living.setHealth(living.getHealth() + 1.0f);
        }

        Vec3 center = new Vec3(x, y, z);
        MobEffect witherResistance = BornInChaosV1ModMobEffects.WITHER_RESISTANCE.get();
        List<Player> players = serverLevel.getEntitiesOfClass(
                Player.class,
                new AABB(center, center).inflate(7.0),
                player -> !player.hasEffect(witherResistance)
        );
        if (players.isEmpty()) {
            return;
        }

        long dayTime = serverLevel.dayTime();
        float damage = dayTime >= 2400000L ? 4.0f : (dayTime >= 1200000L ? 3.0f : 2.0f);
        DamageSource witherDamage = new DamageSource(
                serverLevel.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(DamageTypes.WITHER)
        );

        for (Player player : players) {
            player.hurt(witherDamage, damage);
            player.addEffect(new MobEffectInstance(MobEffects.WITHER, 15, 0));
            serverLevel.sendParticles(
                    BornInChaosV1ModParticleTypes.DARK_SMOKE.get(),
                    player.getX(),
                    player.getY() + 1.5,
                    player.getZ(),
                    5,
                    0.3,
                    0.2,
                    0.3,
                    0.1
            );
        }
    }
}
