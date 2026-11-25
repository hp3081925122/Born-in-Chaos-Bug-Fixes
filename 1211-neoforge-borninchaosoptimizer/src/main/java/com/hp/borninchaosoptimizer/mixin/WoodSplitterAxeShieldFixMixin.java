package com.hp.borninchaosoptimizer.mixin;

import com.hp.borninchaosoptimizer.config.OptimizerConfig;
import com.hp.borninchaosoptimizer.util.Batch5ProcedureHelper;
import net.mcreator.borninchaosv.entity.DoorKnightEntity;
import net.mcreator.borninchaosv.entity.DoorKnightNotDespawnEntity;
import net.mcreator.borninchaosv.entity.SkeletonThrasherEntity;
import net.mcreator.borninchaosv.entity.SkeletonThrasherNotDespawnEntity;
import net.mcreator.borninchaosv.init.BornInChaosV1ModMobEffects;
import net.mcreator.borninchaosv.procedures.WoodSplitterAxePriUdariePoSushchnostiInstrumientomProcedure;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.level.LevelAccessor;
import net.neoforged.neoforge.common.ItemAbilities;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 修复劈木斧重击破盾时缺失受击者与使用手槽位上下文（导致创造模式扣耐久、盾牌碎裂无声无粒子且不解除举盾），并兼容模组自定义盾牌。
 */
@Mixin(value = WoodSplitterAxePriUdariePoSushchnostiInstrumientomProcedure.class, remap = false)
public abstract class WoodSplitterAxeShieldFixMixin {
    @Inject(method = "execute", at = @At("HEAD"), cancellable = true)
    private static void bic$fixWoodSplitterShieldBreak(LevelAccessor world, double x, double y, double z, Entity entity, CallbackInfo ci) {
        if (!OptimizerConfig.isEnabled(OptimizerConfig.FIX_EQUIPMENT_DURABILITY_CONTEXT)) {
            return;
        }
        ci.cancel();
        if (!(entity instanceof LivingEntity target)) {
            return;
        }
        if (entity instanceof DoorKnightEntity
                || entity instanceof DoorKnightNotDespawnEntity
                || entity instanceof SkeletonThrasherEntity
                || entity instanceof SkeletonThrasherNotDespawnEntity) {
            if (!target.hasEffect(BornInChaosV1ModMobEffects.BLOCK_BREAK)) {
                Batch5ProcedureHelper.playSoundBothSides(world, x, y, z, "entity.zombie.break_wooden_door", 0.6f, 1.0f);
                if (world instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(ParticleTypes.CRIT, x, y, z, 5, 0.3, 0.5, 0.3, 0.1);
                }
            }
            if (!target.level().isClientSide()) {
                target.addEffect(new MobEffectInstance(BornInChaosV1ModMobEffects.BLOCK_BREAK, 600, 0, false, false));
            }
        }
        if ((entity instanceof Player || entity instanceof Mob) && target.isBlocking()) {
            ItemStack useItem = target.getUseItem();
            if (useItem.getItem() instanceof ShieldItem || useItem.canPerformAction(ItemAbilities.SHIELD_BLOCK)) {
                net.minecraft.world.item.Item shieldItem = useItem.getItem();
                if (world instanceof ServerLevel) {
                    useItem.hurtAndBreak(500, target, LivingEntity.getSlotForHand(target.getUsedItemHand()));
                }
                if (target instanceof Player player) {
                    player.getCooldowns().addCooldown(shieldItem, 100);
                }
                target.stopUsingItem();
                Batch5ProcedureHelper.playSoundBothSides(world, x, y, z, "entity.zombie.break_wooden_door", 0.6f, 1.0f);
                if (world instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(ParticleTypes.CRIT, x, y, z, 5, 0.3, 0.5, 0.3, 0.1);
                }
            }
        }
    }
}
