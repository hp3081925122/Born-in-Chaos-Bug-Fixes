package com.hp.borninchaosoptimizer.mixin.block;

import com.hp.borninchaosoptimizer.util.BicBlockMapColorHelper;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 在生于混沌方块注册构造阶段一次性为缺失 mapColor 的 109 个可见方块补齐材质对应原版地图色，
 * 零运行期额外开销，同时修复 Xaero 大小地图与原版地图看不见或穿透生于混沌方块的问题。
 */
@Mixin(BlockBehaviour.class)
public abstract class BlockMapColorFixMixin {
    @Inject(method = "<init>", at = @At("RETURN"))
    private void bic$assignMissingBlockMapColor(BlockBehaviour.Properties properties, CallbackInfo ci) {
        if (properties == null) {
            return;
        }
        Class<?> selfClass = this.getClass();
        if (!"net.mcreator.borninchaosv.block".equals(selfClass.getPackageName())) {
            return;
        }
        MapColor mapped = BicBlockMapColorHelper.resolveMapColor(selfClass.getSimpleName());
        if (mapped != null) {
            properties.mapColor(mapped);
        }
    }
}
