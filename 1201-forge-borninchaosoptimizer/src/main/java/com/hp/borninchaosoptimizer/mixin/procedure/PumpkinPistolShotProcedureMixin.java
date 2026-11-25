package com.hp.borninchaosoptimizer.mixin.procedure;

import com.hp.borninchaosoptimizer.config.BicOptimizerConfig;
import com.hp.borninchaosoptimizer.util.ClientTotemDisplayHelper;
import net.mcreator.borninchaosv.entity.PumpkinPistolProjectileEntity;
import net.mcreator.borninchaosv.init.BornInChaosV1ModEntities;
import net.mcreator.borninchaosv.init.BornInChaosV1ModItems;
import net.mcreator.borninchaosv.item.PumpkinhandgunItem;
import net.mcreator.borninchaosv.procedures.PumpkinPistolshotProcedure;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
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
 * 修复南瓜手枪射击过程引用客户端 Minecraft 内部类，以及客户端独立随机扣减耐久与弹药导致双端物品不同步的问题。
 */
@Mixin(value = PumpkinPistolshotProcedure.class, remap = false)
public abstract class PumpkinPistolShotProcedureMixin {
    @Unique
    private static final ResourceLocation BIC_OPT$GUN_FIRE_SOUND_ID =
            ResourceLocation.tryBuild("born_in_chaos_v1", "pumpkinhead_gun_fire");

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
        boolean isCreative = ClientTotemDisplayHelper.isGameMode(entity, GameType.CREATIVE);
        Item bulletItem = BornInChaosV1ModItems.PUMPKIN_BULLET.get();
        boolean hasBullet = entity instanceof Player player && player.getInventory().contains(new ItemStack(bulletItem));
        if (!hasBullet && !isCreative) {
            return;
        }

        Level entityLevel = entity.level();
        if (!entityLevel.isClientSide()) {
            PumpkinPistolProjectileEntity projectile = new PumpkinPistolProjectileEntity(
                    BornInChaosV1ModEntities.PUMPKIN_PISTOL_PROJECTILE.get(), entityLevel);
            projectile.setOwner(entity);
            projectile.setBaseDamage(3.0f);
            projectile.setKnockback(1);
            projectile.setSilent(true);
            projectile.setPos(entity.getX(), entity.getEyeY() - 0.1, entity.getZ());
            Vec3 look = entity.getLookAngle();
            projectile.shoot(look.x, look.y, look.z, 5.0f, 0.0f);
            entityLevel.addFreshEntity(projectile);
        }

        if (world instanceof Level level && BIC_OPT$GUN_FIRE_SOUND_ID != null) {
            SoundEvent sound = ForgeRegistries.SOUND_EVENTS.getValue(BIC_OPT$GUN_FIRE_SOUND_ID);
            if (sound != null) {
                if (!level.isClientSide()) {
                    level.playSound(null, BlockPos.containing(x, y, z), sound, SoundSource.NEUTRAL, 0.9f, 1.0f);
                } else {
                    level.playLocalSound(x, y, z, sound, SoundSource.NEUTRAL, 0.9f, 1.0f, false);
                }
            }
        }

        if (!world.isClientSide() && itemstack.getItem() instanceof PumpkinhandgunItem) {
            itemstack.getOrCreateTag().putString("geckoAnim", "shot");
        }

        Item handgunItem = BornInChaosV1ModItems.PUMPKINHANDGUN.get();
        if (!world.isClientSide() && !isCreative) {
            if (entity instanceof Player player) {
                player.getInventory().clearOrCountMatchingItems(
                        p -> p.getItem() == bulletItem, 1, player.inventoryMenu.getCraftSlots());
            }
            if (entity instanceof LivingEntity living) {
                ServerPlayer sp = entity instanceof ServerPlayer s ? s : null;
                ItemStack mainHand = living.getMainHandItem();
                if (mainHand.getItem() == handgunItem) {
                    if (mainHand.hurt(1, entityLevel.getRandom(), sp)) {
                        mainHand.shrink(1);
                        mainHand.setDamageValue(0);
                    }
                } else {
                    ItemStack offHand = living.getOffhandItem();
                    if (offHand.getItem() == handgunItem && offHand.hurt(1, entityLevel.getRandom(), sp)) {
                        offHand.shrink(1);
                        offHand.setDamageValue(0);
                    }
                }
            }
        }

        if (entity instanceof Player player) {
            boolean hasHat = entity instanceof LivingEntity living
                    && living.getItemBySlot(EquipmentSlot.HEAD).getItem() == BornInChaosV1ModItems.LORD_PUMPKINHEADS_HAT_HELMET.get();
            player.getCooldowns().addCooldown(handgunItem, hasHat ? 20 : 40);
        }
    }
}
