package com.hp.borninchaosoptimizer.mixin.combat;

import com.hp.borninchaosoptimizer.config.BicOptimizerConfig;
import com.hp.borninchaosoptimizer.util.RetaliationGuard;
import net.mcreator.borninchaosv.init.BornInChaosV1ModItems;
import net.mcreator.borninchaosv.init.BornInChaosV1ModParticleTypes;
import net.mcreator.borninchaosv.procedures.SpinyShellArmorKazhdyiTikDliaShliemaProcedure;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.registries.ForgeRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 为刺壳盔甲受击反伤过程添加防重入锁与服务端限定，
 * 并将原 378 行重复 12 次解析附魔 NBT 的分支简化为单次读取，严格保留全部反伤数值。
 */
@Mixin(value = SpinyShellArmorKazhdyiTikDliaShliemaProcedure.class, remap = false)
public abstract class SpinyShellArmorProcedureMixin {
    @Unique
    private static final ResourceLocation BIC_SND_TURTLE_CRACK = ResourceLocation.tryParse("minecraft:entity.turtle.egg_crack");

    @Inject(
            method = "execute(Lnet/minecraftforge/eventbus/api/Event;Lnet/minecraft/world/level/LevelAccessor;DDDLnet/minecraft/world/entity/Entity;Lnet/minecraft/world/entity/Entity;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void bic$fastSpinyShellRetaliation(
            Event event, LevelAccessor world, double x, double y, double z, Entity entity, Entity sourceentity, CallbackInfo ci
    ) {
        if (!BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.enableGlobalFixes)
                || !BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.fixGlobalCombatEvents)) {
            return;
        }
        ci.cancel();
        if (entity == null || sourceentity == null || world.isClientSide()) {
            return;
        }
        if (!(entity instanceof Player player) || !(sourceentity instanceof Mob || sourceentity instanceof Monster)) {
            return;
        }
        ItemStack headStack = player.getItemBySlot(EquipmentSlot.HEAD);
        ItemStack chestStack = player.getItemBySlot(EquipmentSlot.CHEST);
        Item helmetItem = BornInChaosV1ModItems.SPINY_SHELL_ARMOR_HELMET.get();
        Item chestItem = BornInChaosV1ModItems.SPINY_SHELL_ARMOR_CHESTPLATE.get();
        boolean hasHelmet = headStack.getItem() == helmetItem;
        boolean hasChest = chestStack.getItem() == chestItem;
        if (!hasHelmet && !hasChest) {
            return;
        }

        float damage;
        if (player.isBlocking()) {
            damage = 1.0f;
        } else if (hasHelmet && hasChest) {
            boolean headThorns = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.THORNS, headStack) != 0;
            boolean chestThorns = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.THORNS, chestStack) != 0;
            if (headThorns && chestThorns) {
                damage = 10.0f;
            } else if (headThorns || chestThorns) {
                damage = 8.0f;
            } else {
                damage = 4.0f;
            }
        } else if (hasHelmet) {
            boolean headThorns = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.THORNS, headStack) != 0;
            damage = headThorns ? 4.0f : 2.0f;
        } else {
            boolean chestThorns = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.THORNS, chestStack) != 0;
            damage = chestThorns ? 5.0f : 2.0f;
        }

        if (!RetaliationGuard.tryEnter()) {
            return;
        }
        try {
            DamageSource genericDamage = new DamageSource(
                    world.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(DamageTypes.GENERIC)
            );
            sourceentity.hurt(genericDamage, damage);
            if (world instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(
                        BornInChaosV1ModParticleTypes.SPIKERELEASE.get(),
                        x, y + 1.0, z,
                        3, 0.3, 0.3, 0.3, 0.1
                );
            }
            if (world instanceof Level level) {
                SoundEvent sound = ForgeRegistries.SOUND_EVENTS.getValue(BIC_SND_TURTLE_CRACK);
                if (sound != null) {
                    level.playSound(null, BlockPos.containing(x, y, z), sound, SoundSource.NEUTRAL, 0.6f, 0.9f);
                }
            }
        } finally {
            RetaliationGuard.exit();
        }
    }
}
