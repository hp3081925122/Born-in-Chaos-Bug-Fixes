package com.hp.borninchaosoptimizer.mixin;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * 静默过滤 1.21.1 生于混沌 9 个结构 NBT 文件（如 dark_tower、lookout_tower、clown_caravan 等）中
 * 残留的 1.20.1 废弃属性 forge:step_height_addition 与 forge:entity_gravity，消除结构生成时的未知属性日志刷屏。
 */
@Mixin(AttributeMap.class)
public abstract class LegacyForgeAttributeFilterMixin {
    @ModifyVariable(method = "load", at = @At("HEAD"), argsOnly = true)
    private ListTag bic$filterLegacyForgeAttributes(ListTag listTag) {
        if (listTag == null || listTag.isEmpty()) {
            return listTag;
        }
        int size = listTag.size();
        boolean hasLegacy = false;
        for (int i = 0; i < size; i++) {
            String id = listTag.getCompound(i).getString("id");
            if ("forge:step_height_addition".equals(id) || "forge:entity_gravity".equals(id)) {
                hasLegacy = true;
                break;
            }
        }
        if (!hasLegacy) {
            return listTag;
        }
        ListTag filtered = new ListTag();
        for (int i = 0; i < size; i++) {
            CompoundTag entry = listTag.getCompound(i);
            String id = entry.getString("id");
            if (!"forge:step_height_addition".equals(id) && !"forge:entity_gravity".equals(id)) {
                filtered.add(entry);
            }
        }
        return filtered;
    }
}
