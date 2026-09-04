package com.youyimc.createmassenergy.menu;

import com.youyimc.createmassenergy.ModMenuTypes;
import com.youyimc.createmassenergy.block.entity.DataTerminalBlockEntity;
import com.youyimc.createmassenergy.item.UpgradeItem;
import com.youyimc.createmassenergy.upgrade.UpgradeSlot;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.items.SlotItemHandler;

/**
 * 数据化终端 GUI
 * 包含：8 格物品栏 + 6 升级插槽 + 玩家背包
 */
public class DataTerminalMenu extends AbstractContainerMenu {

    private final DataTerminalBlockEntity blockEntity;

    public DataTerminalMenu(int containerId, Inventory playerInventory, DataTerminalBlockEntity blockEntity) {
        super(ModMenuTypes.DATA_TERMINAL.get(), containerId);
        this.blockEntity = blockEntity;

        // 8 格物品栏（1 行 × 8 列，居中，x 起始 16：8*18=144，(176-144)/2=16）
        for (int col = 0; col < 8; col++) {
            this.addSlot(new SlotItemHandler(blockEntity.getInventory(), col, 16 + col * 18, 26));
        }

        // 6 升级插槽（1 行 × 6 列，居中，x 起始 34：6*18=108，(176-108)/2=34）
        for (int col = 0; col < 6; col++) {
            this.addSlot(new UpgradeSlot(blockEntity, blockEntity.getUpgradeSlots(), col, 34 + col * 18, 54));
        }

        // 玩家背包（3 行 × 9 列）
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 96 + row * 18));
            }
        }
        // 玩家快捷栏（9 格）
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 154));
        }
    }

    /** 网络创建构造函数 */
    public DataTerminalMenu(int containerId, Inventory playerInventory, FriendlyByteBuf extraData) {
        this(containerId, playerInventory, getBlockEntity(playerInventory, extraData));
    }

    private static DataTerminalBlockEntity getBlockEntity(Inventory playerInventory, FriendlyByteBuf buf) {
        BlockPos pos = buf.readBlockPos();
        Level level = playerInventory.player.level();
        if (level.getBlockEntity(pos) instanceof DataTerminalBlockEntity be) {
            return be;
        }
        return null;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack stackInSlot = slot.getItem();
            itemStack = stackInSlot.copy();
            int inventoryStart = 0;
            int inventoryEnd = DataTerminalBlockEntity.INVENTORY_SLOTS; // 8
            int upgradeStart = inventoryEnd;
            int upgradeEnd = inventoryEnd + 6; // 14
            if (index < inventoryEnd) {
                if (!this.moveItemStackTo(stackInSlot, inventoryEnd, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (index < upgradeEnd) {
                // 升级插槽中的物品移到背包
                if (!this.moveItemStackTo(stackInSlot, upgradeEnd, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (stackInSlot.getItem() instanceof UpgradeItem) {
                if (!this.moveItemStackTo(stackInSlot, upgradeStart, upgradeEnd, false)) {
                    return ItemStack.EMPTY;
                }
            } else {
                if (!this.moveItemStackTo(stackInSlot, 0, inventoryEnd, false)) {
                    return ItemStack.EMPTY;
                }
            }
            if (stackInSlot.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }
        return itemStack;
    }

    @Override
    public boolean stillValid(Player player) {
        if (blockEntity == null) {
            return false;
        }
        return blockEntity.getLevel() != null
            && blockEntity.getBlockPos().closerToCenterThan(player.position(), 8.0);
    }

    public DataTerminalBlockEntity getBlockEntity() {
        return blockEntity;
    }
}
