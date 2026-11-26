package com.hp.borninchaosoptimizer.mixin.client;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import software.bernie.geckolib.core.molang.MolangParser;

/**
 * 修复生于混沌 4 个 GeckoLib 动画 JSON（babyskeleton、babyskeletondeamon、boneimp 的 idle 动画与
 * scarletpersecutor 的 attack 动画）关键帧中误写单独减号 "-" 导致解析抛出 CompoundException 并损毁动画的问题。
 * 通过在入口处将 "-" 替换为数值 JsonPrimitive(0.0)，走 GeckoLib 原生 Constant 解析路径，
 * 避免使用 GeckoLib 4.8.3 中 variable 为 null 的 MolangParser.ZERO 触发求值 NPE。
 */
@Mixin(value = MolangParser.class, remap = false)
public abstract class GeoAnimDashFixMixin {
    @Unique
    private static final JsonPrimitive BIC$ZERO_JSON = new JsonPrimitive(0.0D);

    @ModifyVariable(method = "parseJson", at = @At("HEAD"), argsOnly = true)
    private static JsonElement bic$handleStrayDashKeyframe(JsonElement element) {
        if (element != null && element.isJsonPrimitive()) {
            JsonPrimitive primitive = element.getAsJsonPrimitive();
            if (primitive.isString() && "-".equals(primitive.getAsString().trim())) {
                return BIC$ZERO_JSON;
            }
        }
        return element;
    }
}
