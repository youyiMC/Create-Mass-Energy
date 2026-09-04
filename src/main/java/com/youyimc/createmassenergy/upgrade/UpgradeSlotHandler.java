package com.youyimc.createmassenergy.upgrade;

import java.util.Set;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemStackHandler;

/**
 * 升级插槽处理器（6 个升级插槽）
 * 支持指定允许的升级类型
 */
public class UpgradeSlotHandler extends ItemStackHandler implements UpgradeContainer {

    private final Set<UpgradeType> allowedTypes;
    private final Runnable onChange;

    public UpgradeSlotHandler(Set<UpgradeType> allowedTypes, Runnable onChange) {
        super(SLOT_COUNT);
        this.allowedTypes = allowedTypes;
        this.onChange = onChange;
    }

    @Override
    protected void onContentsChanged(int slot) {
        super.onContentsChanged(slot);
        if (onChange != null) {
            onChange.run();
        }
    }

    @Override
    public ItemStack getUpgradeStack(int slot) {
        return getStackInSlot(slot);
    }

    @Override
    public void setUpgradeStack(int slot, ItemStack stack) {
        setStackInSlot(slot, stack);
    }

    @Override
    public void setUpgradeChanged() {
        if (onChange != null) {
            onChange.run();
        }
    }

    @Override
    public Set<UpgradeType> getAllowedUpgradeTypes() {
        return allowedTypes;
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        return isValidUpgrade(stack);
    }
}
