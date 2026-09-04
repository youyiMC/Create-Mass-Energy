package com.youyimc.createmassenergy.upgrade;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.SlotItemHandler;

/**
 * 升级插槽槽位（校验升级类型合法性）
 */
public class UpgradeSlot extends SlotItemHandler {

    private final UpgradeContainer container;

    public UpgradeSlot(UpgradeContainer container, UpgradeSlotHandler handler, int index, int xPosition, int yPosition) {
        super(handler, index, xPosition, yPosition);
        this.container = container;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return container.isValidUpgrade(stack);
    }

    @Override
    public boolean mayPickup(Player playerIn) {
        return true;
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }

    @Override
    public void setChanged() {
        super.setChanged();
        container.setUpgradeChanged();
    }
}
