package com.youyimc.createmassenergy.util;

import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.items.IItemHandler;

/**
 * 方块实体物品栏辅助工具
 */
public final class InventoryHelper {
    private InventoryHelper() {}

    /**
     * 将 ItemStackHandler 的内容掉落到世界中
     */
    public static void dropContents(Level level, BlockPos pos, IItemHandler handler) {
        if (level == null) {
            return;
        }
        for (int i = 0; i < handler.getSlots(); i++) {
            ItemStack stack = handler.getStackInSlot(i);
            if (!stack.isEmpty()) {
                net.minecraft.world.entity.item.ItemEntity entity = new net.minecraft.world.entity.item.ItemEntity(level,
                    pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, stack.copy());
                entity.setDefaultPickUpDelay();
                level.addFreshEntity(entity);
            }
        }
    }

    /**
     * 将普通 Container 的内容掉落到世界中
     */
    public static void dropContainerContents(Level level, BlockPos pos, Container container) {
        net.minecraft.world.Containers.dropContents(level, pos, container);
    }
}
