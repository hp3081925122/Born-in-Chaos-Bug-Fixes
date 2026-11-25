package com.hp.borninchaosoptimizer.mixin.entity;

import com.hp.borninchaosoptimizer.config.BicOptimizerConfig;
import net.mcreator.borninchaosv.entity.*;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 修复全部 14 种生于混沌投射物（继承 AbstractArrow）的两大缺陷：
 * 1. 玩家发射时默认 pickup=ALLOWED，贴身发射或盾牌反弹可在落地消失前白嫖刷取材料物品；
 * 2. getPickupItem() 直接返回 static final PROJECTILE_ITEM 单例引用而未 copy()，
 *    一旦被拾取修改 count=0 会导致后续所有同种投射物客户端渲染变成空物品隐形。
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
            method = "<init>(Lnet/minecraft/world/entity/EntityType;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/level/Level;)V",
            at = @At("TAIL")
    )
    private void bic$disallowPlayerPickup(EntityType<? extends AbstractArrow> type, LivingEntity shooter, Level world, CallbackInfo ci) {
        if (BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.enableGlobalFixes)
                && BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.fixProjectilePickupDupe)) {
            this.pickup = AbstractArrow.Pickup.DISALLOWED;
        }
    }

    @Inject(method = "getPickupItem", at = @At("RETURN"), cancellable = true)
    private void bic$defensiveCopyPickupItem(CallbackInfoReturnable<ItemStack> cir) {
        if (!BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.enableGlobalFixes)
                || !BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.fixProjectilePickupDupe)) {
            return;
        }
        ItemStack ret = cir.getReturnValue();
        if (ret != null && !ret.isEmpty()) {
            cir.setReturnValue(ret.copy());
        }
    }
}
