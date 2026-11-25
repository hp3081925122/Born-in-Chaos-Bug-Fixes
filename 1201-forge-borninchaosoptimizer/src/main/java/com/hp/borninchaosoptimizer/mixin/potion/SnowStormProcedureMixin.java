package com.hp.borninchaosoptimizer.mixin.potion;

import com.hp.borninchaosoptimizer.config.BicOptimizerConfig;
import net.mcreator.borninchaosv.entity.KrampusEntity;
import net.mcreator.borninchaosv.entity.KrampusHenchmanEntity;
import net.mcreator.borninchaosv.init.BornInChaosV1ModMobEffects;
import net.mcreator.borninchaosv.init.BornInChaosV1ModParticleTypes;
import net.mcreator.borninchaosv.procedures.SnowStormKazhdyiTikVoVriemiaEffiektaProcedure;
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
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

/**
 * 修复暴风雪药水（Snow Storm）：
 * 1. 修复客户端未判断 isClientSide 直接 setBlock 放置幽灵霜冰方块的 Bug；
 * 2. 修复双端独立递减 freezing NBT 计数与客户端无效 hurt 调用；
 * 3. 消除每个目标高达 16 次重复的 hasEffect/getEffect 查询及无意义的 Stream 距离排序分配。
 */
@Mixin(value = SnowStormKazhdyiTikVoVriemiaEffiektaProcedure.class, remap = false)
public abstract class SnowStormProcedureMixin {
    private static final ResourceLocation HURT_FREEZE_ID = ResourceLocation.tryParse("minecraft:entity.player.hurt_freeze");

    @Inject(method = "execute", at = @At("HEAD"), cancellable = true)
    private static void bicOptimizer$optimizeSnowStorm(LevelAccessor world, double x, double y, double z, Entity entity, CallbackInfo ci) {
        if (!BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.enableGlobalFixes)
                || !BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.optimizePotionAuraAndStateTicks)) {
            return;
        }
        ci.cancel();
        if (entity == null || !(world instanceof ServerLevel serverLevel)) {
            return;
        }

        double ex = entity.getX();
        double ey = entity.getY();
        double ez = entity.getZ();
        serverLevel.sendParticles(BornInChaosV1ModParticleTypes.SNOWCLOUD.get(), ex, ey + 0.2, ez, 3, 1.4, 0.1, 1.4, 0.1);
        serverLevel.sendParticles(BornInChaosV1ModParticleTypes.LITTLESNOWFLAKE.get(), ex, ey + 2.0, ez, 3, 1.0, 0.6, 1.0, 0.2);
        serverLevel.sendParticles(BornInChaosV1ModParticleTypes.WANINGSNOWFLAKE.get(), ex, ey + 2.0, ez, 1, 1.0, 0.6, 1.0, 0.1);

        double freezing = entity.getPersistentData().getDouble("freezing");
        if (freezing <= 0.0) {
            freezing = 40.0;
        } else {
            freezing -= 1.0;
        }
        entity.getPersistentData().putDouble("freezing", freezing);

        if (freezing == 0.0) {
            Vec3 center = new Vec3(ex, ey, ez);
            boolean livingOnly = BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.fixNonLivingAoeAndTrapDamage);
            List<? extends Entity> targets = livingOnly
                    ? serverLevel.getEntitiesOfClass(LivingEntity.class, new AABB(center, center).inflate(3.75))
                    : serverLevel.getEntitiesOfClass(Entity.class, new AABB(center, center).inflate(3.75));
            MobEffect snowStorm = BornInChaosV1ModMobEffects.SNOW_STORM.get();
            MobEffect boneChilling = BornInChaosV1ModMobEffects.BONE_CHILLING.get();
            DamageSource freezeDamage = new DamageSource(
                    serverLevel.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(DamageTypes.FREEZE)
            );
            SoundEvent freezeSound = HURT_FREEZE_ID != null ? ForgeRegistries.SOUND_EVENTS.getValue(HURT_FREEZE_ID) : null;
            LivingEntity ownerLiving = entity instanceof LivingEntity l ? l : null;

            for (Entity target : targets) {
                if (target instanceof KrampusEntity || target instanceof KrampusHenchmanEntity || target instanceof ItemEntity) {
                    continue;
                }
                if (target instanceof LivingEntity targetLiving && targetLiving.hasEffect(snowStorm)) {
                    continue;
                }
                if (ownerLiving != null && target instanceof TamableAnimal tamable && tamable.isOwnedBy(ownerLiving)) {
                    continue;
                }

                float damageAmount;
                if (target instanceof LivingEntity targetLiving) {
                    MobEffectInstance currentChill = targetLiving.getEffect(boneChilling);
                    if (currentChill == null) {
                        targetLiving.addEffect(new MobEffectInstance(boneChilling, 80, 0));
                        damageAmount = 1.0f;
                    } else {
                        int amp = currentChill.getAmplifier();
                        switch (amp) {
                            case 0 -> {
                                targetLiving.addEffect(new MobEffectInstance(boneChilling, 100, 1));
                                damageAmount = 1.0f;
                            }
                            case 1 -> {
                                targetLiving.addEffect(new MobEffectInstance(boneChilling, 200, 2));
                                damageAmount = 1.0f;
                            }
                            case 2 -> {
                                targetLiving.addEffect(new MobEffectInstance(boneChilling, 140, 3));
                                damageAmount = 2.0f;
                            }
                            case 3 -> {
                                targetLiving.addEffect(new MobEffectInstance(boneChilling, 100, 4));
                                damageAmount = 2.0f;
                            }
                            case 4 -> {
                                targetLiving.addEffect(new MobEffectInstance(boneChilling, 100, 5));
                                damageAmount = 3.0f;
                            }
                            case 5 -> {
                                targetLiving.addEffect(new MobEffectInstance(boneChilling, 80, 6));
                                damageAmount = 3.0f;
                            }
                            default -> {
                                targetLiving.addEffect(new MobEffectInstance(boneChilling, 80, 6));
                                damageAmount = 4.0f;
                            }
                        }
                    }
                } else {
                    damageAmount = 1.0f;
                }

                target.hurt(freezeDamage, damageAmount);
                double tx = target.getX();
                double ty = target.getY() + 1.4;
                double tz = target.getZ();
                if (freezeSound != null) {
                    serverLevel.playSound(null, BlockPos.containing(tx, ty, tz), freezeSound, SoundSource.NEUTRAL, 0.8f, 1.0f);
                }
                serverLevel.sendParticles(BornInChaosV1ModParticleTypes.LITTLESNOWFLAKE.get(), tx, ty, tz, 4, 0.25, 0.25, 0.25, 0.1);
                serverLevel.sendParticles(BornInChaosV1ModParticleTypes.WANINGSNOWFLAKE.get(), tx, ty, tz, 2, 0.25, 0.25, 0.25, 0.1);
            }
        }

        if (entity instanceof Player) {
            BlockPos below = BlockPos.containing(x, y - 1.0, z);
            Block belowBlock = serverLevel.getBlockState(below).getBlock();
            if (belowBlock == Blocks.WATER
                    || belowBlock == Blocks.KELP_PLANT
                    || belowBlock == Blocks.SEAGRASS
                    || belowBlock == Blocks.TALL_SEAGRASS) {
                serverLevel.setBlock(below, Blocks.FROSTED_ICE.defaultBlockState(), 3);
            }
        }
    }
}
