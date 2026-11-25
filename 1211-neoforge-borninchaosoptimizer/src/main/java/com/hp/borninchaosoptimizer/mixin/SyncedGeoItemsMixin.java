package com.hp.borninchaosoptimizer.mixin;

import com.hp.borninchaosoptimizer.util.IBicSyncedGeoItem;
import net.mcreator.borninchaosv.item.BonescallerStaffItem;
import net.mcreator.borninchaosv.item.PumpkinhandgunItem;
import net.mcreator.borninchaosv.item.PumpkinstaffaItem;
import net.mcreator.borninchaosv.item.StaffOfMagicArrowsAItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

/**
 * 为原模组 4 种带 GeckoLib 动画的武器统一实现 IBicSyncedGeoItem 接口。
 */
@Mixin(value = {
        StaffOfMagicArrowsAItem.class,
        BonescallerStaffItem.class,
        PumpkinstaffaItem.class,
        PumpkinhandgunItem.class
}, remap = false)
public abstract class SyncedGeoItemsMixin implements IBicSyncedGeoItem {
    @Shadow(remap = false)
    public String animationprocedure;

    @Override
    public void bic$setAnimationProcedure(String animation) {
        this.animationprocedure = animation;
    }
}
