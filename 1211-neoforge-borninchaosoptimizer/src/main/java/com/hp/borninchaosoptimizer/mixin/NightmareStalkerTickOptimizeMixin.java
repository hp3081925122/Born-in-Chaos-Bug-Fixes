package com.hp.borninchaosoptimizer.mixin;

import com.hp.borninchaosoptimizer.config.OptimizerConfig;
import com.hp.borninchaosoptimizer.util.EntityEffectHelper;
import com.hp.borninchaosoptimizer.util.SeasonalDateCache;
import net.mcreator.borninchaosv.entity.NightmareStalkerEntity;
import net.mcreator.borninchaosv.init.BornInChaosV1ModBlocks;
import net.mcreator.borninchaosv.init.BornInChaosV1ModGameRules;
import net.mcreator.borninchaosv.init.BornInChaosV1ModMobEffects;
import net.mcreator.borninchaosv.init.BornInChaosV1ModParticleTypes;
import net.mcreator.borninchaosv.procedures.NightmareStalkerGonProcedure;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 深度优化噩梦潜猎者每刻过程（NightmareStalkerGonProcedure）：
 * 1. 消除每刻多次 Calendar.getInstance() 与最多 8 次 getDisplayName().getString() 文本序列化分配；
 * 2. 仅在贴图实际变化时写入同步实体数据 TEXTURE；
 * 3. 对被动药水（暗影合一、力量、抗性提升）改为低余量刷新，消除每刻重算属性修饰符与全服同步包风暴；
 * 4. 合并脚下方块状态读取，并将阳光消散的客户端/夜晚条件前置短路。
 */
@Mixin(value = NightmareStalkerGonProcedure.class, remap = false)
public abstract class NightmareStalkerTickOptimizeMixin {
    @Inject(method = "execute", at = @At("HEAD"), cancellable = true)
    private static void bic$optimizeNightmareStalkerTick(LevelAccessor world, double x, double y, double z, Entity entity, CallbackInfo ci) {
        if (!OptimizerConfig.isEnabled(OptimizerConfig.OPTIMIZE_SKY_AND_SUNBURN_CHECKS)) {
            return;
        }
        ci.cancel();
        if (!(entity instanceof LivingEntity living)) {
            return;
        }
        if (world.isClientSide()) {
            return;
        }

        float health = living.getHealth();
        float maxHealth = living.getMaxHealth();
        if (health >= maxHealth) {
            EntityEffectHelper.refreshPassiveEffect(living, BornInChaosV1ModMobEffects.UNITY_WITH_DARKNESS, 10, 0, 3);
        }

        boolean hasUnity = living.hasEffect(BornInChaosV1ModMobEffects.UNITY_WITH_DARKNESS);
        boolean loverNamed = bic$isLoverNamed(entity);
        if (entity instanceof NightmareStalkerEntity stalker) {
            String desiredTexture = null;
            if (health >= maxHealth && hasUnity) {
                desiredTexture = "nightmarestalker_e";
            } else if (!hasUnity && health < maxHealth) {
                desiredTexture = (SeasonalDateCache.isValentinesDate() || loverNamed)
                        ? "nightmarestalker_lover"
                        : "nightmarestalker";
            }
            if (desiredTexture != null && !desiredTexture.equals(stalker.getTexture())) {
                stalker.setTexture(desiredTexture);
            }
        }

        long dayTime = world.dayTime();
        if (dayTime >= 1_200_000L && !living.hasEffect(BornInChaosV1ModMobEffects.TERRIFYING_PRESENCE)) {
            if (dayTime >= 2_400_000L) {
                EntityEffectHelper.refreshPassiveEffect(living, MobEffects.DAMAGE_BOOST, 20, 2, 5);
                EntityEffectHelper.refreshPassiveEffect(living, MobEffects.DAMAGE_RESISTANCE, 20, 2, 5);
            } else {
                EntityEffectHelper.refreshPassiveEffect(living, MobEffects.DAMAGE_BOOST, 20, 1, 5);
                EntityEffectHelper.refreshPassiveEffect(living, MobEffects.DAMAGE_RESISTANCE, 20, 0, 5);
            }
        }

        if (dayTime >= 360_000L) {
            BlockPos feetPos = BlockPos.containing(x, y, z);
            if (world.getBlockState(feetPos).is(Blocks.AIR)) {
                BlockPos belowPos = BlockPos.containing(x, y - 1.0, z);
                Block belowBlock = world.getBlockState(belowPos).getBlock();
                if (belowBlock == Blocks.WATER
                        || belowBlock == Blocks.KELP_PLANT
                        || belowBlock == Blocks.SEAGRASS
                        || belowBlock == Blocks.TALL_SEAGRASS) {
                    world.setBlock(belowPos, BornInChaosV1ModBlocks.DARK_ICE.get().defaultBlockState(), 3);
                }
            }
        }

        if (!loverNamed && world instanceof ServerLevel serverLevel) {
            Level level = serverLevel;
            if (level.isDay()
                    && !level.isRaining()
                    && !level.isThundering()
                    && level.getGameRules().getBoolean(BornInChaosV1ModGameRules.DISAPPEARANCEOFSPIRITSUNDERTHESUN)
                    && world.canSeeSkyFromBelowWater(BlockPos.containing(x, y + 1.0, z))) {
                entity.discard();
                serverLevel.sendParticles(BornInChaosV1ModParticleTypes.DIMLARG.get(), x, y, z, 10, 0.5, 0.5, 0.5, 0.1);
            }
        }
    }

    @Unique
    private static boolean bic$isLoverNamed(Entity entity) {
        if (!entity.hasCustomName()) {
            return false;
        }
        String name = entity.getDisplayName().getString();
        return "Pookie".equalsIgnoreCase(name) || "Lover".equalsIgnoreCase(name);
    }
}
