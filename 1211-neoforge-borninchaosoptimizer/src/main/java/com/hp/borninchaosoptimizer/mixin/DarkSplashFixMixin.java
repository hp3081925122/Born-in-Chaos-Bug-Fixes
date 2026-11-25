package com.hp.borninchaosoptimizer.mixin;

import com.hp.borninchaosoptimizer.config.OptimizerConfig;
import com.hp.borninchaosoptimizer.util.EntityEffectHelper;
import java.util.List;
import net.mcreator.borninchaosv.entity.LifestealerEntity;
import net.mcreator.borninchaosv.entity.LifestealerTrueFormEntity;
import net.mcreator.borninchaosv.init.BornInChaosV1ModMobEffects;
import net.mcreator.borninchaosv.init.BornInChaosV1ModParticleTypes;
import net.mcreator.borninchaosv.init.BornInChaosV1ModSounds;
import net.mcreator.borninchaosv.procedures.DarkSplashKazhdyiTikVoVriemiaEffiektaProcedure;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 修复暗影溅射（Dark Splash）每刻刷抗性包及爆炸时扫描 Entity.class 误毁地面掉落物/经验球/展示框的缺陷，并省去流式排序开销。
 */
@Mixin(value = DarkSplashKazhdyiTikVoVriemiaEffiektaProcedure.class, remap = false)
public abstract class DarkSplashFixMixin {
    @Inject(method = "execute", at = @At("HEAD"), cancellable = true)
    private static void bic$fixDarkSplashTickAndTargets(LevelAccessor world, double x, double y, double z, Entity entity, CallbackInfo ci) {
        if (!OptimizerConfig.isEnabled(OptimizerConfig.OPTIMIZE_EFFECT_TICK_SCANS)) {
            return;
        }
        ci.cancel();
        if (!(entity instanceof LivingEntity caster) || !caster.isAlive() || caster.getHealth() <= 15.0f) {
            return;
        }
        EntityEffectHelper.refreshPassiveEffect(caster, MobEffects.DAMAGE_RESISTANCE, 70, 3, 10);

        double charge = entity.getPersistentData().getDouble("chargedsplash");
        charge = (charge == 0.0) ? 50.0 : (charge - 1.0);
        entity.getPersistentData().putDouble("chargedsplash", charge);
        if (charge != 0.0) {
            return;
        }

        caster.removeEffect(BornInChaosV1ModMobEffects.DARK_SPLASH);
        if (!caster.level().isClientSide()) {
            caster.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 80, 0, false, false));
            caster.addEffect(new MobEffectInstance(BornInChaosV1ModMobEffects.BLOCK_BREAK, 260, 0, false, false));
        }
        caster.setHealth(Math.min(caster.getMaxHealth(), caster.getHealth() + 10.0f));

        if (world instanceof Level level) {
            if (!level.isClientSide()) {
                level.playSound(null, BlockPos.containing(x, y, z), BornInChaosV1ModSounds.LIFESTEALER_SCREAM_AP.get(), SoundSource.NEUTRAL, 2.0f, 1.0f);
            } else {
                level.playLocalSound(x, y, z, BornInChaosV1ModSounds.LIFESTEALER_SCREAM_AP.get(), SoundSource.NEUTRAL, 2.0f, 1.0f, false);
            }
        }
        if (world instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(BornInChaosV1ModParticleTypes.DIM_LONG.get(), x, y + 1.5, z, 25, 0.5, 0.7, 0.5, 0.5);
        }

        AABB area = new AABB(x - 5.5, y - 5.5, z - 5.5, x + 5.5, y + 5.5, z + 5.5);
        DamageSource witherSource = new DamageSource(world.holderOrThrow(DamageTypes.WITHER), entity);
        Vec3 pushVec = new Vec3(
                2.0 * Math.sin(Math.toRadians(entity.getYRot() + 180.0f)),
                0.0,
                2.0 * Math.cos(Math.toRadians(entity.getYRot()))
        );
        List<LivingEntity> victims = world.getEntitiesOfClass(LivingEntity.class, area, target ->
                target != entity
                        && !(target instanceof LifestealerTrueFormEntity)
                        && !(target instanceof LifestealerEntity)
                        && !target.hasEffect(BornInChaosV1ModMobEffects.WITHER_RESISTANCE)
        );
        for (LivingEntity victim : victims) {
            victim.hurt(witherSource, 10.0f);
            if (!victim.level().isClientSide()) {
                victim.addEffect(new MobEffectInstance(BornInChaosV1ModMobEffects.LIFESTEAL, 260, 0));
            }
            if (world instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(BornInChaosV1ModParticleTypes.DARKMATTER.get(), victim.getX(), victim.getY() + 0.5, victim.getZ(), 4, 0.4, 0.8, 0.4, 0.1);
            }
            victim.setDeltaMovement(pushVec);
        }
    }
}
