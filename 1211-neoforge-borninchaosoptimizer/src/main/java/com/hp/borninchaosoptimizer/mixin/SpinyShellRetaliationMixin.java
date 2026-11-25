package com.hp.borninchaosoptimizer.mixin;

import com.hp.borninchaosoptimizer.config.OptimizerConfig;
import com.hp.borninchaosoptimizer.util.CombatGuard;
import net.mcreator.borninchaosv.init.BornInChaosV1ModItems;
import net.mcreator.borninchaosv.init.BornInChaosV1ModParticleTypes;
import net.mcreator.borninchaosv.procedures.SpinyShellArmorKazhdyiTikDliaShliemaProcedure;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.neoforged.bus.api.Event;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 优化刺壳盔甲反伤过程：增加重入保护防止反伤死循环，消除未穿刺壳甲时的冗余槽位读取，并将穿戴时的 8 次附魔注册表查询合并为 1 次。
 */
@Mixin(value = SpinyShellArmorKazhdyiTikDliaShliemaProcedure.class, remap = false)
public abstract class SpinyShellRetaliationMixin {
    @Inject(method = "execute(Lnet/neoforged/bus/api/Event;Lnet/minecraft/world/level/LevelAccessor;DDDLnet/minecraft/world/entity/Entity;Lnet/minecraft/world/entity/Entity;)V", at = @At("HEAD"), cancellable = true)
    private static void bic$optimizeSpinyShellRetaliation(Event event, LevelAccessor world, double x, double y, double z, Entity entity, Entity sourceentity, CallbackInfo ci) {
        if (!OptimizerConfig.isEnabled(OptimizerConfig.FIX_THORNS_REENTRANT_AND_LOOKUP)) {
            return;
        }
        ci.cancel();
        if (!(entity instanceof Player player) || !(sourceentity instanceof Mob)) {
            return;
        }
        ItemStack head = player.getItemBySlot(EquipmentSlot.HEAD);
        ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
        boolean hasHelmet = head.getItem() == BornInChaosV1ModItems.SPINY_SHELL_ARMOR_HELMET.get();
        boolean hasChest = chest.getItem() == BornInChaosV1ModItems.SPINY_SHELL_ARMOR_CHESTPLATE.get();
        if (!hasHelmet && !hasChest) {
            return;
        }
        if (!CombatGuard.enterRetaliation()) {
            return;
        }
        try {
            float damage;
            if (player.isBlocking()) {
                damage = 1.0f;
            } else {
                Holder<Enchantment> thornsHolder = world.registryAccess()
                        .lookupOrThrow(Registries.ENCHANTMENT)
                        .getOrThrow(Enchantments.THORNS);
                boolean headThorns = hasHelmet && head.getEnchantmentLevel(thornsHolder) != 0;
                boolean chestThorns = hasChest && chest.getEnchantmentLevel(thornsHolder) != 0;
                if (hasHelmet && hasChest) {
                    if (headThorns && chestThorns) {
                        damage = 10.0f;
                    } else if (headThorns || chestThorns) {
                        damage = 8.0f;
                    } else {
                        damage = 4.0f;
                    }
                } else if (hasHelmet) {
                    damage = headThorns ? 4.0f : 2.0f;
                } else {
                    damage = chestThorns ? 5.0f : 2.0f;
                }
            }
            sourceentity.hurt(new DamageSource(world.holderOrThrow(DamageTypes.GENERIC)), damage);
            if (world instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(BornInChaosV1ModParticleTypes.SPIKERELEASE.get(), x, y + 1.0, z, 3, 0.3, 0.3, 0.3, 0.1);
            }
            if (world instanceof Level level) {
                if (!level.isClientSide()) {
                    level.playSound(null, BlockPos.containing(x, y, z), SoundEvents.TURTLE_EGG_CRACK, SoundSource.NEUTRAL, 0.6f, 0.9f);
                } else {
                    level.playLocalSound(x, y, z, SoundEvents.TURTLE_EGG_CRACK, SoundSource.NEUTRAL, 0.6f, 0.9f, false);
                }
            }
        } finally {
            CombatGuard.exitRetaliation();
        }
    }
}
