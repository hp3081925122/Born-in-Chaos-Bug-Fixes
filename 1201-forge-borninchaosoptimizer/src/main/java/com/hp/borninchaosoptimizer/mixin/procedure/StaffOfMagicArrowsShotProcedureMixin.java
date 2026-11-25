package com.hp.borninchaosoptimizer.mixin.procedure;

import com.hp.borninchaosoptimizer.config.BicOptimizerConfig;
import com.hp.borninchaosoptimizer.util.ClientTotemDisplayHelper;
import net.mcreator.borninchaosv.entity.StaffofMagicArrowsProjectileEntity;
import net.mcreator.borninchaosv.init.BornInChaosV1ModEntities;
import net.mcreator.borninchaosv.init.BornInChaosV1ModItems;
import net.mcreator.borninchaosv.init.BornInChaosV1ModParticleTypes;
import net.mcreator.borninchaosv.item.StaffOfMagicArrowsAItem;
import net.mcreator.borninchaosv.procedures.StaffofMagicArrowsPriIspolzovaniiStrielkovoghoPriedmietaProcedure;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 修复魔箭法杖射击过程引用客户端 Minecraft 内部类，以及客户端独立随机扣减耐久导致双端耐久不同步的问题。
 */
@Mixin(value = StaffofMagicArrowsPriIspolzovaniiStrielkovoghoPriedmietaProcedure.class, remap = false)
public abstract class StaffOfMagicArrowsShotProcedureMixin {
    @Unique
    private static final ResourceLocation BIC_OPT$STAFF_SHOOT_SOUND_ID =
            ResourceLocation.tryBuild("born_in_chaos_v1", "magic_staff_shoot");

    @Inject(method = "execute", at = @At("HEAD"), cancellable = true)
    private static void bicOpt$safeExecute(LevelAccessor world, double x, double y, double z, Entity entity, ItemStack itemstack, CallbackInfo ci) {
        if (!BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.enableGlobalFixes)
                || !BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.fixDedicatedServerClientRefs)) {
            return;
        }
        ci.cancel();
        if (entity == null) {
            return;
        }

        Item dustItem = BornInChaosV1ModItems.SPIRITUAL_DUST.get();
        Item staffItem = BornInChaosV1ModItems.STAFF_OF_MAGIC_ARROWS.get();
        boolean hasDust = entity instanceof Player player && player.getInventory().contains(new ItemStack(dustItem));
        boolean isCreative = ClientTotemDisplayHelper.isGameMode(entity, GameType.CREATIVE);

        if (hasDust && !world.isClientSide() && !isCreative && entity instanceof Player player) {
            player.getInventory().clearOrCountMatchingItems(
                    p -> p.getItem() == dustItem, 1, player.inventoryMenu.getCraftSlots());
        }

        Level entityLevel = entity.level();
        if (!entityLevel.isClientSide()) {
            StaffofMagicArrowsProjectileEntity projectile = new StaffofMagicArrowsProjectileEntity(
                    BornInChaosV1ModEntities.STAFFOF_MAGIC_ARROWS_PROJECTILE.get(), entityLevel);
            projectile.setOwner(entity);
            projectile.setBaseDamage(hasDust ? 3.0f : 1.8f);
            projectile.setKnockback(1);
            projectile.setSilent(true);
            projectile.setPierceLevel((byte) 5);
            projectile.setPos(entity.getX(), entity.getEyeY() - 0.1, entity.getZ());
            Vec3 look = entity.getLookAngle();
            projectile.shoot(look.x, look.y, look.z, 5.0f, 0.0f);
            entityLevel.addFreshEntity(projectile);
        }

        if (world instanceof Level level && BIC_OPT$STAFF_SHOOT_SOUND_ID != null) {
            SoundEvent sound = ForgeRegistries.SOUND_EVENTS.getValue(BIC_OPT$STAFF_SHOOT_SOUND_ID);
            if (sound != null) {
                if (!level.isClientSide()) {
                    level.playSound(null, BlockPos.containing(x, y, z), sound, SoundSource.NEUTRAL, 0.6f, 1.0f);
                } else {
                    level.playLocalSound(x, y, z, sound, SoundSource.NEUTRAL, 0.6f, 1.0f, false);
                }
            }
        }

        if (hasDust && world instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(BornInChaosV1ModParticleTypes.RITUAL.get(), x, y + 1.0, z, 5, 0.3, 0.3, 0.3, 0.1);
        }

        if (entity instanceof LivingEntity living) {
            ServerPlayer sp = entity instanceof ServerPlayer s ? s : null;
            ItemStack mainHand = living.getMainHandItem();
            if (mainHand.getItem() == staffItem) {
                if (!world.isClientSide() && itemstack.getItem() instanceof StaffOfMagicArrowsAItem) {
                    itemstack.getOrCreateTag().putString("geckoAnim", "shot_right");
                }
                living.swing(InteractionHand.MAIN_HAND, true);
                if (!world.isClientSide() && mainHand.hurt(1, entityLevel.getRandom(), sp)) {
                    mainHand.shrink(1);
                    mainHand.setDamageValue(0);
                }
            } else {
                ItemStack offHand = living.getOffhandItem();
                if (offHand.getItem() == staffItem) {
                    if (!world.isClientSide() && itemstack.getItem() instanceof StaffOfMagicArrowsAItem) {
                        itemstack.getOrCreateTag().putString("geckoAnim", "shot_left");
                    }
                    living.swing(InteractionHand.OFF_HAND, true);
                    if (!world.isClientSide() && offHand.hurt(1, entityLevel.getRandom(), sp)) {
                        offHand.shrink(1);
                        offHand.setDamageValue(0);
                    }
                }
            }
        }

        if (entity instanceof Player player) {
            boolean fullNightmareSet = entity instanceof LivingEntity living
                    && living.getItemBySlot(EquipmentSlot.HEAD).getItem() == BornInChaosV1ModItems.NIGHTMARE_MANTLEOFTHE_NIGHT_HELMET.get()
                    && living.getItemBySlot(EquipmentSlot.LEGS).getItem() == BornInChaosV1ModItems.NIGHTMARE_MANTLEOFTHE_NIGHT_LEGGINGS.get()
                    && living.getItemBySlot(EquipmentSlot.CHEST).getItem() == BornInChaosV1ModItems.NIGHTMARE_MANTLEOFTHE_NIGHT_CHESTPLATE.get()
                    && living.getItemBySlot(EquipmentSlot.FEET).getItem() == BornInChaosV1ModItems.NIGHTMARE_MANTLEOFTHE_NIGHT_BOOTS.get();
            player.getCooldowns().addCooldown(staffItem, fullNightmareSet ? 25 : 40);
        }
    }
}
