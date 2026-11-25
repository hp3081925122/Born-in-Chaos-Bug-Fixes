package com.hp.borninchaosoptimizer.mixin.block;

import com.hp.borninchaosoptimizer.config.BicOptimizerConfig;
import net.mcreator.borninchaosv.entity.BloodyGadflyEntity;
import net.mcreator.borninchaosv.entity.CorpseFlyEntity;
import net.mcreator.borninchaosv.entity.DoorKnightEntity;
import net.mcreator.borninchaosv.entity.DoorKnightNotDespawnEntity;
import net.mcreator.borninchaosv.entity.SkeletonThrasherEntity;
import net.mcreator.borninchaosv.entity.SkeletonThrasherNotDespawnEntity;
import net.mcreator.borninchaosv.init.BornInChaosV1ModItems;
import net.mcreator.borninchaosv.init.BornInChaosV1ModMobEffects;
import net.mcreator.borninchaosv.procedures.SpinyShellTrapPriStolknovieniiSushchnostiSBlokomProcedure;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 修复棘壳陷阱实体碰撞过程：
 * 1. 修复 !(entity instanceof CorpseFlyEntity) || !(entity instanceof BloodyGadflyEntity) 恒真逻辑笔误（改为 &&）；
 * 2. 限制仅在服务端执行伤害判定。
 */
@Mixin(value = SpinyShellTrapPriStolknovieniiSushchnostiSBlokomProcedure.class, remap = false)
public abstract class SpinyShellTrapProcedureMixin {
    @Inject(method = "execute", at = @At("HEAD"), cancellable = true)
    private static void bicOpt$fixFlyImmunityAndServerOnly(LevelAccessor world, Entity entity, CallbackInfo ci) {
        if (!BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.enableGlobalFixes)
                || !BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.optimizeStaticTrapBlockTicks)) {
            return;
        }
        ci.cancel();
        if (entity == null || world.isClientSide()) {
            return;
        }
        if (entity instanceof CorpseFlyEntity || entity instanceof BloodyGadflyEntity) {
            return;
        }
        if (!(entity instanceof Mob) && !(entity instanceof Monster)) {
            if (!(entity instanceof Player player)) {
                return;
            }
            if (player.getItemBySlot(EquipmentSlot.HEAD).getItem() == BornInChaosV1ModItems.SPINY_SHELL_ARMOR_HELMET.get()
                    && player.getItemBySlot(EquipmentSlot.CHEST).getItem() == BornInChaosV1ModItems.SPINY_SHELL_ARMOR_CHESTPLATE.get()) {
                return;
            }
        }
        entity.hurt(new DamageSource(world.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(DamageTypes.CACTUS)), 2.0f);
        if (entity instanceof LivingEntity living
                && (entity instanceof DoorKnightEntity
                || entity instanceof SkeletonThrasherEntity
                || entity instanceof SkeletonThrasherNotDespawnEntity
                || entity instanceof DoorKnightNotDespawnEntity)) {
            living.addEffect(new MobEffectInstance(BornInChaosV1ModMobEffects.BLOCK_BREAK.get(), 60, 0, false, false));
        }
    }
}
