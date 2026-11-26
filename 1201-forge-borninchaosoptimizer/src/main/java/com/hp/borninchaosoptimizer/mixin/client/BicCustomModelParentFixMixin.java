package com.hp.borninchaosoptimizer.mixin.client;

import com.google.gson.JsonObject;
import net.minecraft.client.renderer.block.model.BlockModel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 清理生于混沌 55 个 models/custom/*.json 自包含模型中多余的无命名空间自引用 parent
 * （如 "parent": "grudbone"、"houndtrap"、"Icysweetness"），消除启动与重载资源时的模型缺失刷屏报错。
 * 由于原版 ModelManager 在异步线程中直接调用 BlockModel.fromStream 反序列化模型，
 * 此处采用无状态线程安全判断：任何自带 elements 的模型若 parent 既不含命名空间 ":" 又不在 block/、item/、builtin/ 下，
 * 在 Minecraft 资产体系中均属于必然缺失的非法裸名引用，直接置空即可安全跳过。
 */
@Mixin(BlockModel.Deserializer.class)
public abstract class BicCustomModelParentFixMixin {
    @Inject(method = "getParentName", at = @At("RETURN"), cancellable = true)
    private void bic$stripBrokenSelfParent(JsonObject json, CallbackInfoReturnable<String> cir) {
        String parent = cir.getReturnValue();
        if (parent == null || parent.isEmpty() || !json.has("elements")) {
            return;
        }
        if (!parent.contains(":")
                && !parent.startsWith("block/")
                && !parent.startsWith("item/")
                && !parent.startsWith("builtin/")) {
            cir.setReturnValue("");
        }
    }
}
