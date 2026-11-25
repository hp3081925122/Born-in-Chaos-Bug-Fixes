package com.hp.borninchaosoptimizer.mixin.scoreboard;

import com.hp.borninchaosoptimizer.config.BicOptimizerConfig;
import net.mcreator.borninchaosv.entity.ThornshellCrabEntity;
import net.mcreator.borninchaosv.init.BornInChaosV1ModEntities;
import net.mcreator.borninchaosv.init.BornInChaosV1ModItems;
import net.mcreator.borninchaosv.init.BornInChaosV1ModMobEffects;
import net.mcreator.borninchaosv.procedures.ThornshellCrabPriObnovlieniiTikaSushchnostiProcedure;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
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
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 替换荆棘蟹每 tick 过程对原版 Scoreboard("eat") 的匿名内部类查询，
 * 使用实体 PersistentData 并在客户端直接短路，消除每只螃蟹每 tick 的双端冗余执行。
 */
@Mixin(value = ThornshellCrabPriObnovlieniiTikaSushchnostiProcedure.class, remap = false)
public abstract class ThornshellCrabTickProcedureMixin {
    @Unique
    private static final String BIC_KEY_EAT = "bic_opt_eat";

    @Inject(method = "execute", at = @At("HEAD"), cancellable = true)
    private static void bic$replaceScoreboardTick(
            LevelAccessor world, double x, double y, double z, Entity entity, CallbackInfo ci
    ) {
        if (!BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.enableGlobalFixes)
                || !BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.fixScoreboardEntityLeak)) {
            return;
        }
        ci.cancel();
        if (entity == null || world.isClientSide() || !(entity instanceof LivingEntity living)) {
            return;
        }
        CompoundTag pdata = entity.getPersistentData();
        if (pdata.getBoolean(BIC_KEY_EAT) && living.hasEffect(BornInChaosV1ModMobEffects.BARBEDATTACK.get())) {
            pdata.putBoolean(BIC_KEY_EAT, false);
            if (entity instanceof ThornshellCrabEntity crab) {
                crab.setAnimation("eat");
            }
        }
        if (living.hasEffect(MobEffects.SATURATION)) {
            double eati = pdata.getDouble("eati");
            if (eati == 0.0) {
                eati = 10.0;
            } else {
                eati -= 1.0;
            }
            pdata.putDouble("eati", eati);
            if (eati == 0.0) {
                if (world instanceof ServerLevel serverLevel) {
                    ItemStack drop = Math.random() < 0.35
                            ? new ItemStack(BornInChaosV1ModItems.SPINY_SHELL.get())
                            : new ItemStack(Items.BONE_MEAL);
                    ItemEntity itemEntity = new ItemEntity(serverLevel, x, y, z, drop);
                    itemEntity.setPickUpDelay(10);
                    serverLevel.addFreshEntity(itemEntity);
                }
                pdata.putDouble("eati", 0.0);
                living.removeEffect(MobEffects.SATURATION);
            }
        }
        if (living.isBaby() && !entity.isRemoved()) {
            entity.discard();
            if (world instanceof ServerLevel serverLevel) {
                Entity spawned = BornInChaosV1ModEntities.THORNSHELL_CRAB.get()
                        .spawn(serverLevel, BlockPos.containing(x, y, z), MobSpawnType.MOB_SUMMONED);
                if (spawned != null) {
                    spawned.setYRot(entity.getYRot());
                    spawned.setYBodyRot(entity.getYRot());
                    spawned.setYHeadRot(entity.getYRot());
                    spawned.setXRot(entity.getXRot());
                }
            }
            return;
        }
        if (entity.isInWater()) {
            living.addEffect(new MobEffectInstance(MobEffects.DOLPHINS_GRACE, 40, 3, false, false));
        }
    }
}
