package com.hp.borninchaosoptimizer.mixin.entity;

import com.hp.borninchaosoptimizer.config.BicOptimizerConfig;
import net.mcreator.borninchaosv.entity.*;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;

/**
 * 修复全部 14 种生于混沌投射物（继承 AbstractArrow）的两大缺陷：
 * 1. 玩家发射时三参构造器或二参构造器配合 setOwner(Player) 会将 pickup 设为 ALLOWED，贴身发射或盾牌反弹可白嫖刷取材料物品；
 * 2. 原 getPickupItem() 直接返回 static final PROJECTILE_ITEM 单例引用，一旦被拾取修改 count=0 会污染静态常量导致后续同种投射物渲染隐形。
 *    现统一强制 pickup=DISALLOWED、拦截 tryPickup 并在 getPickupItem 返回空栈（不影响客户端渲染所用的 ItemSupplier.getItem）。
 */
@Mixin({
        IntoxicatindBombProjectileEntity.class,
        MissionaryChargeEntity.class,
        MonstrousEasterEggProjectileEntity.class,
        PhantomBombProjectileEntity.class,
        PumpkinPistol2ProjectileEntity.class,
        PumpkinPistolProjectileEntity.class,
        PumpkinStaff2ProjectileEntity.class,
        PumpkinStaffProjectileEntity.class,
        RottenEasterEggProjectileEntity.class,
        SpiritualEasterEggProjectileEntity.class,
        StaffofBlindnessProjectileEntity.class,
        StaffofMagicArrows2ProjectileEntity.class,
        StaffofMagicArrowsProjectileEntity.class,
        StimulatingBombprojectileEntity.class
})
public abstract class BicProjectilesMixin extends AbstractArrow {
    protected BicProjectilesMixin(EntityType<? extends AbstractArrow> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(
            method = "<init>(Lnet/minecraft/world/entity/EntityType;Lnet/minecraft/world/level/Level;)V",
            at = @At("TAIL")
    )
    private void bic$disallowPickupTwoArg(EntityType<? extends AbstractArrow> type, Level world, CallbackInfo ci) {
        if (BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.enableGlobalFixes)
                && BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.fixProjectilePickupDupe)) {
            this.pickup = AbstractArrow.Pickup.DISALLOWED;
        }
    }

    @Inject(
            method = "<init>(Lnet/minecraft/world/entity/EntityType;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/level/Level;)V",
            at = @At("TAIL")
    )
    private void bic$disallowPlayerPickup(EntityType<? extends AbstractArrow> type, LivingEntity shooter, Level world, CallbackInfo ci) {
        if (BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.enableGlobalFixes)
                && BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.fixProjectilePickupDupe)) {
            this.pickup = AbstractArrow.Pickup.DISALLOWED;
        }
    }

    @Override
    public void setOwner(@Nullable Entity owner) {
        super.setOwner(owner);
        if (BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.enableGlobalFixes)
                && BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.fixProjectilePickupDupe)) {
            this.pickup = AbstractArrow.Pickup.DISALLOWED;
        }
    }

    @Override
    protected boolean tryPickup(Player player) {
        if (BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.enableGlobalFixes)
                && BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.fixProjectilePickupDupe)) {
            return false;
        }
        return super.tryPickup(player);
    }

    @Inject(method = "getPickupItem", at = @At("HEAD"), cancellable = true)
    private void bic$emptyPickupItem(CallbackInfoReturnable<ItemStack> cir) {
        if (BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.enableGlobalFixes)
                && BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.fixProjectilePickupDupe)) {
            cir.setReturnValue(ItemStack.EMPTY);
        }
    }
}
