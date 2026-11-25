package com.hp.borninchaosoptimizer.mixin;

import com.hp.borninchaosoptimizer.config.OptimizerConfig;
import net.mcreator.borninchaosv.entity.ZombieLumberjackEntity;
import net.mcreator.borninchaosv.procedures.ZombieLumberjackAtackProcedure;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.neoforged.bus.api.Event;
import net.neoforged.neoforge.common.ItemAbilities;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 修复伐木工僵尸破盾时缺少持有者与槽位上下文（导致创造模式扣耐久、盾牌碎裂无声无粒子），以及盾牌碎裂变成空气后将空气加入冷却列表的缺陷。
 */
@Mixin(value = ZombieLumberjackAtackProcedure.class, remap = false)
public abstract class ZombieLumberjackAttackFixMixin {
    @Inject(method = "execute(Lnet/neoforged/bus/api/Event;Lnet/minecraft/world/level/LevelAccessor;DDDLnet/minecraft/world/entity/Entity;Lnet/minecraft/world/entity/Entity;)V", at = @At("HEAD"), cancellable = true)
    private static void bic$fixLumberjackShieldBreak(Event event, LevelAccessor world, double x, double y, double z, Entity entity, Entity sourceentity, CallbackInfo ci) {
        if (!OptimizerConfig.isEnabled(OptimizerConfig.FIX_EQUIPMENT_DURABILITY_CONTEXT)) {
            return;
        }
        ci.cancel();
        if (!(sourceentity instanceof ZombieLumberjackEntity) || !(entity instanceof LivingEntity target)) {
            return;
        }
        if (!(entity instanceof Mob || (entity instanceof Player && target.isBlocking()))) {
            return;
        }
        ItemStack useItem = target.getUseItem();
        if (useItem.getItem() instanceof ShieldItem || useItem.canPerformAction(ItemAbilities.SHIELD_BLOCK)) {
            Item shieldItem = useItem.getItem();
            if (world instanceof ServerLevel) {
                useItem.hurtAndBreak(500, target, LivingEntity.getSlotForHand(target.getUsedItemHand()));
            }
            if (target instanceof Player player) {
                player.getCooldowns().addCooldown(shieldItem, 100);
            }
            target.stopUsingItem();
            if (world instanceof Level level) {
                if (!level.isClientSide()) {
                    level.playSound(null, BlockPos.containing(x, y, z), SoundEvents.ZOMBIE_BREAK_WOODEN_DOOR, SoundSource.NEUTRAL, 0.6f, 1.0f);
                } else {
                    level.playLocalSound(x, y, z, SoundEvents.ZOMBIE_BREAK_WOODEN_DOOR, SoundSource.NEUTRAL, 0.6f, 1.0f, false);
                }
            }
        }
    }
}
