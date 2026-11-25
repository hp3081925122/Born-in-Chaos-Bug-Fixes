package com.hp.borninchaosoptimizer.mixin;

import com.hp.borninchaosoptimizer.config.OptimizerConfig;
import net.mcreator.borninchaosv.entity.IntoxicatindBombProjectileEntity;
import net.mcreator.borninchaosv.entity.MissionaryChargeEntity;
import net.mcreator.borninchaosv.entity.MonstrousEasterEggProjectileEntity;
import net.mcreator.borninchaosv.entity.PhantomBombProjectileEntity;
import net.mcreator.borninchaosv.entity.PumpkinPistol2ProjectileEntity;
import net.mcreator.borninchaosv.entity.PumpkinPistolProjectileEntity;
import net.mcreator.borninchaosv.entity.PumpkinStaff2ProjectileEntity;
import net.mcreator.borninchaosv.entity.PumpkinStaffProjectileEntity;
import net.mcreator.borninchaosv.entity.RottenEasterEggProjectileEntity;
import net.mcreator.borninchaosv.entity.SpiritualEasterEggProjectileEntity;
import net.mcreator.borninchaosv.entity.StaffofBlindnessProjectileEntity;
import net.mcreator.borninchaosv.entity.StaffofMagicArrows2ProjectileEntity;
import net.mcreator.borninchaosv.entity.StaffofMagicArrowsProjectileEntity;
import net.mcreator.borninchaosv.entity.StimulatingBombprojectileEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 修复全部 14 种继承 AbstractArrow 的生于混沌弹射物在玩家发射时默认 pickup=ALLOWED，
 * 导致玩家贴身发射或盾牌反弹可在落地销毁前触碰白嫖灵尘、幻影炸弹与复活节彩蛋的刷物品漏洞。
 */
@Mixin(value = {
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
public abstract class ProjectilePickupDupeFixMixin extends AbstractArrow {
    protected ProjectilePickupDupeFixMixin(EntityType<? extends AbstractArrow> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(
            method = "<init>(Lnet/minecraft/world/entity/EntityType;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/ItemStack;)V",
            at = @At("TAIL")
    )
    private void bic$disallowShooterPickup(EntityType<? extends AbstractArrow> type, LivingEntity shooter, Level world, ItemStack weapon, CallbackInfo ci) {
        if (OptimizerConfig.isEnabled(OptimizerConfig.FIX_PROJECTILE_PICKUP_DUPE)) {
            this.pickup = AbstractArrow.Pickup.DISALLOWED;
        }
    }

    @Inject(
            method = "<init>(Lnet/minecraft/world/entity/EntityType;Lnet/minecraft/world/level/Level;)V",
            at = @At("TAIL")
    )
    private void bic$disallowDefaultPickup(EntityType<? extends AbstractArrow> type, Level world, CallbackInfo ci) {
        if (OptimizerConfig.isEnabled(OptimizerConfig.FIX_PROJECTILE_PICKUP_DUPE)) {
            this.pickup = AbstractArrow.Pickup.DISALLOWED;
        }
    }
}
