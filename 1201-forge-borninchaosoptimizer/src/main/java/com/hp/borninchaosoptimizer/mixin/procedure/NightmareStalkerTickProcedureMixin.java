package com.hp.borninchaosoptimizer.mixin.procedure;

import com.hp.borninchaosoptimizer.config.BicOptimizerConfig;
import com.hp.borninchaosoptimizer.util.SeasonalDateCache;
import net.mcreator.borninchaosv.entity.NightmareStalkerEntity;
import net.mcreator.borninchaosv.init.BornInChaosV1ModBlocks;
import net.mcreator.borninchaosv.init.BornInChaosV1ModGameRules;
import net.mcreator.borninchaosv.init.BornInChaosV1ModMobEffects;
import net.mcreator.borninchaosv.init.BornInChaosV1ModParticleTypes;
import net.mcreator.borninchaosv.procedures.NightmareStalkerGonProcedure;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 优化梦魇潜行者每 tick 更新过程：
 * 1. 仅服务端权威执行，消除客户端放置幽灵暗冰方块；
 * 2. 替换每 tick 多次 Calendar.getInstance() 为 SeasonalDateCache；
 * 3. 仅在存在自定义名称时解析字符串，消除每 tick 8 次 getDisplayName().getString() 分配；
 * 4. 贴图未变化时不重复写入同步数据，并对日光消散采用白天晴天前置与错峰检测。
 */
@Mixin(value = NightmareStalkerGonProcedure.class, remap = false)
public abstract class NightmareStalkerTickProcedureMixin {
    @Inject(method = "execute", at = @At("HEAD"), cancellable = true)
    private static void bicOpt$optimizeNightmareTick(LevelAccessor world, double x, double y, double z, Entity entity, CallbackInfo ci) {
        if (!BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.enableGlobalFixes)
                || !BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.optimizeSunAndSkyTickProcedures)) {
            return;
        }
        ci.cancel();
        if (!(entity instanceof LivingEntity living) || world.isClientSide() || !(world instanceof ServerLevel serverLevel)) {
            return;
        }

        float health = living.getHealth();
        float maxHealth = living.getMaxHealth();
        MobEffect unityWithDarkness = BornInChaosV1ModMobEffects.UNITY_WITH_DARKNESS.get();

        if (health >= maxHealth) {
            living.addEffect(new MobEffectInstance(unityWithDarkness, 10, 0, false, false));
        }

        boolean hasUnity = living.hasEffect(unityWithDarkness);
        boolean isLoverNamed = bicOpt$isLoverName(entity);

        if (entity instanceof NightmareStalkerEntity stalker) {
            String targetTex = null;
            if (health >= maxHealth && hasUnity) {
                targetTex = "nightmarestalker_e";
            } else if (!hasUnity && health < maxHealth) {
                if (SeasonalDateCache.isNightmareLoverTextureWindow() || isLoverNamed) {
                    targetTex = "nightmarestalker_lover";
                } else {
                    targetTex = "nightmarestalker";
                }
            }
            if (targetTex != null && !targetTex.equals(stalker.getTexture())) {
                stalker.setTexture(targetTex);
            }
        }

        long dayTime = serverLevel.dayTime();
        if (dayTime >= 1200000L && !living.hasEffect(BornInChaosV1ModMobEffects.TERRIFYING_PRESENCE.get())) {
            if (dayTime >= 2400000L) {
                living.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 20, 2, false, false));
                living.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 20, 2, false, false));
            } else {
                living.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 20, 1, false, false));
                living.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 20, 0, false, false));
            }
        }

        if (dayTime >= 360000L) {
            BlockPos feetPos = BlockPos.containing(x, y, z);
            if (serverLevel.getBlockState(feetPos).getBlock() == Blocks.AIR) {
                BlockPos belowPos = BlockPos.containing(x, y - 1.0, z);
                Block below = serverLevel.getBlockState(belowPos).getBlock();
                if (below == Blocks.WATER || below == Blocks.KELP_PLANT || below == Blocks.SEAGRASS || below == Blocks.TALL_SEAGRASS) {
                    serverLevel.setBlock(belowPos, BornInChaosV1ModBlocks.DARK_ICE.get().defaultBlockState(), 3);
                }
            }
        }

        int interval = Math.max(1, BicOptimizerConfig.getInt(BicOptimizerConfig.COMMON.sunCheckInterval));
        if (!entity.isRemoved()
                && !isLoverNamed
                && serverLevel.isDay()
                && !serverLevel.getLevelData().isRaining()
                && !serverLevel.getLevelData().isThundering()
                && (entity.tickCount + entity.getId()) % interval == 0
                && serverLevel.getGameRules().getBoolean(BornInChaosV1ModGameRules.DISAPPEARANCEOFSPIRITSUNDERTHESUN)
                && serverLevel.canSeeSkyFromBelowWater(BlockPos.containing(x, y + 1.0, z))) {
            entity.discard();
            serverLevel.sendParticles(BornInChaosV1ModParticleTypes.DIMLARG.get(), x, y, z, 10, 0.5, 0.5, 0.5, 0.1);
        }
    }

    @Unique
    private static boolean bicOpt$isLoverName(Entity entity) {
        if (!entity.hasCustomName()) {
            return false;
        }
        Component customName = entity.getCustomName();
        if (customName == null) {
            return false;
        }
        String name = customName.getString();
        return "Pookie".equals(name) || "pookie".equals(name) || "Lover".equals(name) || "lover".equals(name);
    }
}
