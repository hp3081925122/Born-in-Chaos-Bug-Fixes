package com.hp.borninchaosoptimizer.mixin.gecko;

import com.hp.borninchaosoptimizer.config.BicOptimizerConfig;
import net.mcreator.borninchaosv.init.ItemAnimationFactory;
import net.mcreator.borninchaosv.item.BonescallerStaffItem;
import net.mcreator.borninchaosv.item.PumpkinhandgunItem;
import net.mcreator.borninchaosv.item.PumpkinstaffaItem;
import net.mcreator.borninchaosv.item.StaffOfMagicArrowsAItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 修复 ItemAnimationFactory.animatedItems 在检查阶段与物品类型前，
 * 每名玩家每 tick 双端（START + END）无条件执行主副手 ItemStack.copy() 深拷贝完整 NBT 树的性能浪费，
 * 并避免 getOrCreateTag() 对无动画标记的物品凭空写入空 CompoundTag 污染堆叠与触发手部重绘抖动。
 */
@Mixin(value = ItemAnimationFactory.class, remap = false)
public abstract class ItemAnimationFactoryMixin {
    @Inject(method = "animatedItems", at = @At("HEAD"), cancellable = true)
    private static void bic$fastAnimatedItems(TickEvent.PlayerTickEvent event, CallbackInfo ci) {
        if (!BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.enableGlobalFixes)
                || !BicOptimizerConfig.getBoolean(BicOptimizerConfig.COMMON.optimizeAnimationFactories)) {
            return;
        }
        ci.cancel();
        if (event == null || event.phase != TickEvent.Phase.START || event.player == null) {
            return;
        }
        boolean clientSide = event.player.level().isClientSide();
        bic$processHandStack(event.player.getMainHandItem(), clientSide);
        bic$processHandStack(event.player.getOffhandItem(), clientSide);
    }

    @Unique
    private static void bic$processHandStack(ItemStack stack, boolean clientSide) {
        if (stack.isEmpty() || !stack.hasTag()) {
            return;
        }
        Item item = stack.getItem();
        if (!(item instanceof StaffOfMagicArrowsAItem)
                && !(item instanceof BonescallerStaffItem)
                && !(item instanceof PumpkinstaffaItem)
                && !(item instanceof PumpkinhandgunItem)) {
            return;
        }
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains("geckoAnim")) {
            return;
        }
        String animation = tag.getString("geckoAnim");
        if (animation.isEmpty()) {
            return;
        }
        tag.putString("geckoAnim", "");
        if (clientSide) {
            if (item instanceof StaffOfMagicArrowsAItem staff) {
                staff.animationprocedure = animation;
            } else if (item instanceof BonescallerStaffItem staff) {
                staff.animationprocedure = animation;
            } else if (item instanceof PumpkinstaffaItem staff) {
                staff.animationprocedure = animation;
            } else if (item instanceof PumpkinhandgunItem gun) {
                gun.animationprocedure = animation;
            }
        }
    }
}
