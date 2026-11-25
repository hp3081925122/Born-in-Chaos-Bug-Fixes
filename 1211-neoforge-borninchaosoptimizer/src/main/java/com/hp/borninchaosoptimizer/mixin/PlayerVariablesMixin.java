package com.hp.borninchaosoptimizer.mixin;

import net.mcreator.borninchaosv.network.BornInChaosV1ModVariables;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 修复网络数据包解码收到空 CompoundTag 时触发空指针崩溃的隐患，并对异常非有限数值做保护。
 */
@Mixin(value = BornInChaosV1ModVariables.PlayerVariables.class, remap = false)
public abstract class PlayerVariablesMixin {
    @Shadow(remap = false)
    public double naughtiness;

    @Inject(method = "deserializeNBT(Lnet/minecraft/core/HolderLookup$Provider;Lnet/minecraft/nbt/CompoundTag;)V", at = @At("HEAD"), cancellable = true)
    private void bic$safeDeserializeNbt(HolderLookup.Provider lookupProvider, CompoundTag nbt, CallbackInfo ci) {
        ci.cancel();
        if (nbt == null) {
            this.naughtiness = 0.0;
            return;
        }
        double val = nbt.getDouble("naughtiness");
        this.naughtiness = Double.isFinite(val) ? Math.max(0.0, val) : 0.0;
    }
}
