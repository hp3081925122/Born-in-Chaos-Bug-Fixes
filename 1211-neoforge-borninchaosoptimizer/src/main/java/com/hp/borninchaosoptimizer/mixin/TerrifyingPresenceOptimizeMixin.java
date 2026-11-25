package com.hp.borninchaosoptimizer.mixin;

import com.hp.borninchaosoptimizer.config.OptimizerConfig;
import com.hp.borninchaosoptimizer.util.EntityEffectHelper;
import java.util.List;
import net.mcreator.borninchaosv.init.BornInChaosV1ModMobEffects;
import net.mcreator.borninchaosv.init.BornInChaosV1ModParticleTypes;
import net.mcreator.borninchaosv.procedures.TerrifyingPresenceKazhdyiTikVoVriemiaEffiektaProcedure;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 优化骇人威压（Terrifying Presence）每刻过程：
 * 1. 避免每刻无条件调用 removeEffect(DAMAGE_BOOST) 与重复设置相同的 STEP_HEIGHT 属性基值；
 * 2. 对抗性提升改为低余量刷新；
 * 3. 范围凋零光环从扫描全部 Entity.class + 距离排序直接收敛为仅查询 Player.class。
 */
@Mixin(value = TerrifyingPresenceKazhdyiTikVoVriemiaEffiektaProcedure.class, remap = false)
public abstract class TerrifyingPresenceOptimizeMixin {
    @Inject(method = "execute", at = @At("HEAD"), cancellable = true)
    private static void bic$optimizeTerrifyingPresenceTick(LevelAccessor world, double x, double y, double z, Entity entity, CallbackInfo ci) {
        if (!OptimizerConfig.isEnabled(OptimizerConfig.OPTIMIZE_EFFECT_TICK_SCANS)) {
            return;
        }
        ci.cancel();
        if (!(entity instanceof LivingEntity living)) {
            return;
        }
        if (!living.isAlive()) {
            EntityEffectHelper.removeEffectIfPresent(living, BornInChaosV1ModMobEffects.TERRIFYING_PRESENCE);
            return;
        }
        EntityEffectHelper.removeEffectIfPresent(living, MobEffects.DAMAGE_BOOST);
        AttributeInstance stepAttr = living.getAttribute(Attributes.STEP_HEIGHT);
        if (stepAttr != null && stepAttr.getBaseValue() != 3.0) {
            stepAttr.setBaseValue(3.0);
        }
        EntityEffectHelper.refreshPassiveEffect(living, MobEffects.DAMAGE_RESISTANCE, 10, 2, 3);

        if (world instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(BornInChaosV1ModParticleTypes.DARKMATTER.get(), entity.getX(), entity.getY() + 2.0, entity.getZ(), 1, 0.3, 0.3, 0.3, 0.3);
            serverLevel.sendParticles(BornInChaosV1ModParticleTypes.DARKSPOTS.get(), entity.getX(), entity.getY() + 0.3, entity.getZ(), 3, 2.5, 0.1, 2.5, 0.2);
        }

        double timer = entity.getPersistentData().getDouble("terrifyingpresence");
        timer = (timer == 0.0) ? 30.0 : (timer - 1.0);
        entity.getPersistentData().putDouble("terrifyingpresence", timer);
        if (timer != 0.0) {
            return;
        }

        if (living.getHealth() > 10.0f) {
            living.setHealth(Math.min(living.getMaxHealth(), living.getHealth() + 1.0f));
        }

        AABB playerBox = new AABB(x - 7.0, y - 7.0, z - 7.0, x + 7.0, y + 7.0, z + 7.0);
        List<Player> players = world.getEntitiesOfClass(Player.class, playerBox, player ->
                player != entity && !player.hasEffect(BornInChaosV1ModMobEffects.WITHER_RESISTANCE)
        );
        if (players.isEmpty()) {
            return;
        }
        long dayTime = world.dayTime();
        float damage = dayTime >= 2_400_000L ? 4.0f : (dayTime >= 1_200_000L ? 3.0f : 2.0f);
        DamageSource witherSource = new DamageSource(world.holderOrThrow(DamageTypes.WITHER));
        for (Player player : players) {
            player.hurt(witherSource, damage);
            if (!player.level().isClientSide()) {
                player.addEffect(new MobEffectInstance(MobEffects.WITHER, 15, 0));
            }
            if (world instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(BornInChaosV1ModParticleTypes.DARK_SMOKE.get(), player.getX(), player.getY() + 1.5, player.getZ(), 5, 0.3, 0.2, 0.3, 0.1);
            }
        }
    }
}
