package com.hp.borninchaosoptimizer.mixin.core;

import net.mcreator.borninchaosv.network.BornInChaosV1ModVariables;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 防止 PlayerVariables.readNBT 在收到 null 或非 CompoundTag 时抛出空指针或强转异常。
 */
@Mixin(value = BornInChaosV1ModVariables.PlayerVariables.class, remap = false)
public abstract class PlayerVariablesMixin {
    @Inject(method = "readNBT", at = @At("HEAD"), cancellable = true)
    private void bic$guardNullTag(Tag tag, CallbackInfo ci) {
        if (!(tag instanceof CompoundTag)) {
            ci.cancel();
        }
    }
}
