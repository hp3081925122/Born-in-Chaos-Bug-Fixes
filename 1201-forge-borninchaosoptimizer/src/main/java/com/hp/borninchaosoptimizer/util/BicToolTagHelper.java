package com.hp.borninchaosoptimizer.util;

import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraftforge.common.ToolActions;

/**
 * 标准工具标签与方块状态属性复制工具类，替代硬编码 instanceof AxeItem 判断。
 */
public final class BicToolTagHelper {
    private BicToolTagHelper() {
    }

    /**
     * 判断物品栈是否属于斧类（原版 AxeItem、标准 minecraft:axes 标签或 Forge AXE_STRIP 工具动作）。
     */
    public static boolean isStandardAxe(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        return stack.getItem() instanceof AxeItem
                || stack.is(ItemTags.AXES)
                || stack.canPerformAction(ToolActions.AXE_STRIP);
    }

    /**
     * 将源方块状态上的同名兼容属性复制到目标方块状态（如朝向、轴向、含水等）。
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static BlockState copySharedProperties(BlockState source, BlockState target) {
        BlockState result = target;
        for (Property<?> prop : source.getProperties()) {
            Property targetProp = result.getBlock().getStateDefinition().getProperty(prop.getName());
            if (targetProp != null && result.hasProperty(targetProp)) {
                try {
                    result = result.setValue(targetProp, (Comparable) source.getValue(prop));
                } catch (IllegalArgumentException ignored) {
                }
            }
        }
        return result;
    }
}
