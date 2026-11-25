package com.hp.borninchaosoptimizer.mixin.potion;

import com.hp.borninchaosoptimizer.config.BicOptimizerConfig;
import net.mcreator.borninchaosv.entity.LifestealerEntity;
import net.mcreator.borninchaosv.entity.LifestealerTrueFormEntity;
import net.mcreator.borninchaosv.init.BornInChaosV1ModMobEffects;
import net.mcreator.borninchaosv.init.BornInChaosV1ModParticleTypes;
import net.mcreator.borninchaosv.procedures.DarkSplashKazhdyiTikVoVriemiaEffiektaProcedure;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

/**
 * 修复暗影溅射（Dark Splash）双端不同步改血/改速度/改 NBT 问题，移除无用流式排序，
 * 并在服务端设置击退向量后标记 hurtMarked 确保玩家客户端可靠同步击退动量。
 */
@Mixin(value = DarkSplashKazhdyiTikVoVriemiaEffiektaProcedure.class, remap = false)
public abstract class DarkSplashProcedureMixin {
    private static final ResourceLocation SCREAM_SOUND_ID = ResourceLocation.tryParse("born_in_chaos_v1:lifestealer_scream_ap");

    @Inject(method = "execute", at = @At("HEAD"), cancellable = true)
    private static void bicOptimizer$optimizeDarkSplash(LevelAccessor world, double x, double y, double z, Entity entity, CallbackInfo ci) {
        if (!BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.enableGlobalFixes)
                || !BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.optimizePotionAuraAndStateTicks)) {
            return;
        }
        ci.cancel();
        if (!(entity instanceof LivingEntity caster) || !caster.isAlive() || !(world instanceof ServerLevel serverLevel)) {
            return;
        }
        if (caster.getHealth() <= 15.0f) {
            return;
        }

        caster.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 70, 3, false, false));
        double timer = caster.getPersistentData().getDouble("chargedsplash");
        if (timer <= 0.0) {
            timer = 50.0;
        } else {
            timer -= 1.0;
        }
        caster.getPersistentData().putDouble("chargedsplash", timer);

        if (timer != 0.0) {
            return;
        }

        caster.removeEffect(BornInChaosV1ModMobEffects.DARK_SPLASH.get());
        caster.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 80, 0, false, false));
        caster.addEffect(new MobEffectInstance(BornInChaosV1ModMobEffects.BLOCK_BREAK.get(), 260, 0, false, false));
        caster.setHealth(caster.getHealth() + 10.0f);

        SoundEvent screamSound = SCREAM_SOUND_ID != null ? ForgeRegistries.SOUND_EVENTS.getValue(SCREAM_SOUND_ID) : null;
        if (screamSound != null) {
            serverLevel.playSound(null, BlockPos.containing(x, y, z), screamSound, SoundSource.NEUTRAL, 2.0f, 1.0f);
        }
        serverLevel.sendParticles(BornInChaosV1ModParticleTypes.DIM_LONG.get(), x, y + 1.5, z, 25, 0.5, 0.7, 0.5, 0.5);

        Vec3 center = new Vec3(x, y, z);
        boolean livingOnly = BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.fixNonLivingAoeAndTrapDamage);
        List<? extends Entity> targets = livingOnly
                ? serverLevel.getEntitiesOfClass(LivingEntity.class, new AABB(center, center).inflate(5.5))
                : serverLevel.getEntitiesOfClass(Entity.class, new AABB(center, center).inflate(5.5));
        MobEffect witherResistance = BornInChaosV1ModMobEffects.WITHER_RESISTANCE.get();
        MobEffect lifesteal = BornInChaosV1ModMobEffects.LIFESTEAL.get();
        DamageSource witherDamage = new DamageSource(
                serverLevel.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(DamageTypes.WITHER),
                caster
        );
        Vec3 pushVec = new Vec3(
                2.0 * Math.sin(Math.toRadians(caster.getYRot() + 180.0f)),
                0.0,
                2.0 * Math.cos(Math.toRadians(caster.getYRot()))
        );

        for (Entity target : targets) {
            if (target == caster || target instanceof LifestealerTrueFormEntity || target instanceof LifestealerEntity) {
                continue;
            }
            if (target instanceof LivingEntity targetLiving && targetLiving.hasEffect(witherResistance)) {
                continue;
            }
            target.hurt(witherDamage, 10.0f);
            if (target instanceof LivingEntity targetLiving) {
                targetLiving.addEffect(new MobEffectInstance(lifesteal, 260, 0));
            }
            serverLevel.sendParticles(
                    BornInChaosV1ModParticleTypes.DARKMATTER.get(),
                    target.getX(),
                    target.getY() + 0.5,
                    target.getZ(),
                    4,
                    0.4,
                    0.8,
                    0.4,
                    0.1
            );
            target.setDeltaMovement(pushVec);
            target.hurtMarked = true;
        }
    }
}
