package com.hp.borninchaosoptimizer.mixin.client;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import software.bernie.geckolib.loading.math.MathParser;
import software.bernie.geckolib.loading.math.MathValue;
import software.bernie.geckolib.loading.math.value.Constant;

/**
 * 修复生于混沌 4 个 GeckoLib 动画 JSON（babyskeleton、babyskeletondeamon、boneimp 的 idle 动画与
 * scarletpersecutor 的 attack 动画）关键帧中误写单独减号 "-" 导致解析抛出 CompoundException 并损毁动画的问题。
 */
@Mixin(value = MathParser.class, remap = false)
public abstract class GeoAnimDashFixMixin {
    @Inject(method = "parseJson", at = @At("HEAD"), cancellable = true)
    private static void bic$handleStrayDashKeyframe(JsonElement element, CallbackInfoReturnable<MathValue> cir) {
        if (element instanceof JsonPrimitive primitive && primitive.isString() && "-".equals(primitive.getAsString().trim())) {
            cir.setReturnValue(new Constant(0.0));
        }
    }
}
