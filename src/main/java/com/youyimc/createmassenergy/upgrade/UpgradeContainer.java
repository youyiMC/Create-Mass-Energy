package com.youyimc.createmassenergy.upgrade;

import java.util.Set;

import com.youyimc.createmassenergy.item.UpgradeItem;

import net.minecraft.world.item.ItemStack;

/**
 * 升级插槽容器接口
 * 所有功能方块均为 6 个升级插槽。
 */
public interface UpgradeContainer {

    /** 升级插槽数量 */
    int SLOT_COUNT = 6;

    /**
     * 获取指定插槽中的升级物品
     */
    ItemStack getUpgradeStack(int slot);

    /**
     * 设置指定插槽中的升级物品
     */
    void setUpgradeStack(int slot, ItemStack stack);

    /**
     * 设置插槽内容变更
     */
    void setUpgradeChanged();

    /**
     * 该机器允许的升级类型集合
     */
    Set<UpgradeType> getAllowedUpgradeTypes();

    /**
     * 计算总线程数（所有线程升级模块的线程数相加）
     * 默认 1 线程（无升级时）
     */
    default int getTotalThreads() {
        int threads = 0;
        for (int i = 0; i < SLOT_COUNT; i++) {
            ItemStack stack = getUpgradeStack(i);
            if (stack.getItem() instanceof UpgradeItem upgrade) {
                if (upgrade.getDefinition().type() == UpgradeType.THREAD) {
                    threads += (int) upgrade.getDefinition().value();
                }
            }
        }
        return Math.max(1, threads);
    }

    /**
     * 计算总存储容量（字节），所有存储升级模块容量相加
     * 默认返回 0（无存储能力），由机器基础容量自行决定
     */
    default long getTotalStorageCapacity() {
        long capacity = 0;
        for (int i = 0; i < SLOT_COUNT; i++) {
            ItemStack stack = getUpgradeStack(i);
            if (stack.getItem() instanceof UpgradeItem upgrade) {
                if (upgrade.getDefinition().type() == UpgradeType.STORAGE) {
                    capacity += upgrade.getDefinition().value();
                }
            }
        }
        return capacity;
    }

    /**
     * 判断指定物品是否可作为升级模块放入该机器
     */
    default boolean isValidUpgrade(ItemStack stack) {
        if (stack.isEmpty() || !(stack.getItem() instanceof UpgradeItem upgrade)) {
            return false;
        }
        return getAllowedUpgradeTypes().contains(upgrade.getDefinition().type());
    }
}
