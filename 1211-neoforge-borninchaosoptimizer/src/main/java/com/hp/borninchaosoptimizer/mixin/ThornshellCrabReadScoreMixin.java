package com.hp.borninchaosoptimizer.mixin;

import com.hp.borninchaosoptimizer.config.OptimizerConfig;
import net.mcreator.borninchaosv.entity.ThornshellCrabEntity;
import net.mcreator.borninchaosv.init.BornInChaosV1ModEntities;
import net.mcreator.borninchaosv.init.BornInChaosV1ModItems;
import net.mcreator.borninchaosv.init.BornInChaosV1ModMobEffects;
import net.mcreator.borninchaosv.procedures.ThornshellCrabPriObnovlieniiTikaSushchnostiProcedure;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.LevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 修复刺壳蟹每刻更新时查询全局计分板及水中每刻重复创建海豚的恩惠效果实例的问题。
 */
@Mixin(value = ThornshellCrabPriObnovlieniiTikaSushchnostiProcedure.class, remap = false)
public abstract class ThornshellCrabReadScoreMixin {
    @Inject(method = "execute", at = @At("HEAD"), cancellable = true)
    private static void bic$replaceScoreboardReadAndOptimizeTick(LevelAccessor world, double x, double y, double z, Entity entity, CallbackInfo ci) {
        if (!OptimizerConfig.isEnabled(OptimizerConfig.FIX_SCOREBOARD_LEAK)) {
            return;
        }
        ci.cancel();
        if (!(entity instanceof LivingEntity living)) {
            return;
        }
        if (entity.getPersistentData().getBoolean("bic_eat") && living.hasEffect(BornInChaosV1ModMobEffects.BARBEDATTACK)) {
            entity.getPersistentData().putBoolean("bic_eat", false);
            if (entity instanceof ThornshellCrabEntity crab) {
                crab.setAnimation("eat");
            }
        }
        if (living.hasEffect(MobEffects.SATURATION)) {
            double eati = entity.getPersistentData().getDouble("eati");
            if (eati == 0.0) {
                eati = 10.0;
            } else {
                eati -= 1.0;
            }
            entity.getPersistentData().putDouble("eati", eati);
            if (eati == 0.0) {
                if (world instanceof ServerLevel serverLevel) {
                    ItemStack drop = Math.random() < 0.35
                            ? new ItemStack(BornInChaosV1ModItems.SPINY_SHELL.get())
                            : new ItemStack(Items.BONE_MEAL);
                    ItemEntity itemEntity = new ItemEntity(serverLevel, x, y, z, drop);
                    itemEntity.setPickUpDelay(10);
                    serverLevel.addFreshEntity(itemEntity);
                }
                entity.getPersistentData().putDouble("eati", 0.0);
                living.removeEffect(MobEffects.SATURATION);
            }
        }
        if (living.isBaby()) {
            if (world instanceof ServerLevel serverLevel) {
                Entity spawned = BornInChaosV1ModEntities.THORNSHELL_CRAB.get().spawn(serverLevel, BlockPos.containing(x, y, z), MobSpawnType.MOB_SUMMONED);
                if (spawned != null) {
                    spawned.setYRot(entity.getYRot());
                    spawned.setYBodyRot(entity.getYRot());
                    spawned.setYHeadRot(entity.getYRot());
                    spawned.setXRot(entity.getXRot());
                }
            }
            if (!entity.level().isClientSide()) {
                entity.discard();
                return;
            }
        }
        if (entity.isInWater() && !living.level().isClientSide()) {
            MobEffectInstance current = living.getEffect(MobEffects.DOLPHINS_GRACE);
            if (current == null || current.getDuration() <= 10 || current.getAmplifier() < 3) {
                living.addEffect(new MobEffectInstance(MobEffects.DOLPHINS_GRACE, 40, 3, false, false));
            }
        }
    }
}
