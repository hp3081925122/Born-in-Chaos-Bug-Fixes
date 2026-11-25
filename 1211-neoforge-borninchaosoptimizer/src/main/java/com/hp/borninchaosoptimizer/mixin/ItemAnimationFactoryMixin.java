package com.hp.borninchaosoptimizer.mixin;

import com.hp.borninchaosoptimizer.config.OptimizerConfig;
import com.hp.borninchaosoptimizer.util.IBicSyncedGeoItem;
import net.mcreator.borninchaosv.init.ItemAnimationFactory;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 消除 ItemAnimationFactory 每玩家每刻无条件深拷贝主副手 ItemStack 与 CompoundTag 的内存分配。
 */
@Mixin(value = ItemAnimationFactory.class, remap = false)
public abstract class ItemAnimationFactoryMixin {
    @Inject(method = "animatedItems", at = @At("HEAD"), cancellable = true)
    private static void bic$fastItemAnimationTick(PlayerTickEvent.Post event, CallbackInfo ci) {
        if (!OptimizerConfig.isEnabled(OptimizerConfig.OPTIMIZE_ITEM_ANIMATION_SYNC)) {
            return;
        }
        ci.cancel();
        Player player = event.getEntity();
        if (player == null) {
            return;
        }
        boolean clientSide = player.level().isClientSide();
        bic$processHandItem(player.getMainHandItem(), clientSide);
        bic$processHandItem(player.getOffhandItem(), clientSide);
    }

    @Unique
    @SuppressWarnings("deprecation")
    private static void bic$processHandItem(ItemStack stack, boolean clientSide) {
        if (stack.isEmpty() || !(stack.getItem() instanceof IBicSyncedGeoItem geoItem)) {
            return;
        }
        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData == null || !customData.contains("geckoAnim")) {
            return;
        }
        String animation = customData.getUnsafe().getString("geckoAnim");
        if (!animation.isEmpty()) {
            CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.putString("geckoAnim", ""));
            if (clientSide) {
                geoItem.bic$setAnimationProcedure(animation);
            }
        }
    }
}
