package com.hp.borninchaosoptimizer.mixin.potion;

import com.hp.borninchaosoptimizer.config.BicOptimizerConfig;
import net.mcreator.borninchaosv.entity.MissionaryRaiderEntity;
import net.mcreator.borninchaosv.entity.MissionerEntity;
import net.mcreator.borninchaosv.init.BornInChaosV1ModMobEffects;
import net.mcreator.borninchaosv.init.BornInChaosV1ModParticleTypes;
import net.mcreator.borninchaosv.procedures.StunningStrikeKazhdyiTikVoVriemiaEffiektaProcedure;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

/**
 * 修复传教士震荡打击（Stunning Strike）：
 * 1. 客户端状态不同步与无用距离流排序分配；
 * 2. 误伤施法者自身、袭击传教士（MissionaryRaiderEntity）及同阵营灾厄村民；
 * 3. 对周围地面掉落物、经验球、画框造成 9 点伤害导致战利品被炸毁。
 */
@Mixin(value = StunningStrikeKazhdyiTikVoVriemiaEffiektaProcedure.class, remap = false)
public abstract class StunningStrikeProcedureMixin {
    @Inject(method = "execute", at = @At("HEAD"), cancellable = true)
    private static void bicOptimizer$optimizeStunningStrike(LevelAccessor world, Entity entity, CallbackInfo ci) {
        if (!BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.enableGlobalFixes)
                || !BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.optimizePotionAuraAndStateTicks)) {
            return;
        }
        ci.cancel();
        if (entity == null || !(world instanceof ServerLevel serverLevel)) {
            return;
        }

        double timer = entity.getPersistentData().getDouble("stunningstrike");
        if (timer <= 0.0) {
            timer = 20.0;
        } else {
            timer -= 1.0;
        }
        entity.getPersistentData().putDouble("stunningstrike", timer);

        if (timer != 0.0) {
            return;
        }

        if (entity instanceof LivingEntity living) {
            living.addEffect(new MobEffectInstance(BornInChaosV1ModMobEffects.BLOCK_BREAK.get(), 500, 0, false, false));
            living.removeEffect(BornInChaosV1ModMobEffects.STUNNING_STRIKE.get());
        }

        double ex = entity.getX();
        double ey = entity.getY();
        double ez = entity.getZ();
        serverLevel.sendParticles(BornInChaosV1ModParticleTypes.CLOUDSOFDUST.get(), ex, ey + 0.3, ez, 20, 2.0, 0.1, 2.0, 0.1);

        Vec3 center = new Vec3(ex, ey, ez);
        boolean filterNonLiving = BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.fixNonLivingAoeAndTrapDamage);
        MobEffect stun = BornInChaosV1ModMobEffects.STUN.get();
        DamageSource genericDamage = new DamageSource(
                serverLevel.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(DamageTypes.GENERIC)
        );

        if (filterNonLiving) {
            boolean casterIsRaider = entity instanceof Raider;
            List<LivingEntity> targets = serverLevel.getEntitiesOfClass(LivingEntity.class, new AABB(center, center).inflate(4.5));
            for (LivingEntity targetLiving : targets) {
                if (targetLiving == entity
                        || targetLiving instanceof MissionerEntity
                        || targetLiving instanceof MissionaryRaiderEntity
                        || (casterIsRaider && targetLiving instanceof Raider)
                        || targetLiving.getMobType() == MobType.UNDEAD) {
                    continue;
                }
                targetLiving.addEffect(new MobEffectInstance(stun, 40, 0));
                targetLiving.hurt(genericDamage, 9.0f);
            }
        } else {
            List<Entity> targets = serverLevel.getEntitiesOfClass(Entity.class, new AABB(center, center).inflate(4.5));
            for (Entity target : targets) {
                if (target instanceof MissionerEntity) {
                    continue;
                }
                if (target instanceof LivingEntity targetLiving) {
                    if (targetLiving.getMobType() == MobType.UNDEAD) {
                        continue;
                    }
                    targetLiving.addEffect(new MobEffectInstance(stun, 40, 0));
                }
                target.hurt(genericDamage, 9.0f);
            }
        }
    }
}
