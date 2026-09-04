package com.youyimc.createmassenergy.menu;

import com.youyimc.createmassenergy.ModMenuTypes;
import com.youyimc.createmassenergy.block.entity.AnnihilationFurnaceBlockEntity;

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
 * 物品湮灭炉 GUI
 * 显示输入物品槽 + 玩家背包
 */
public class AnnihilationFurnaceMenu extends AbstractContainerMenu {

    private final AnnihilationFurnaceBlockEntity blockEntity;

    public AnnihilationFurnaceMenu(int containerId, Inventory playerInventory,
        AnnihilationFurnaceBlockEntity blockEntity) {
        super(ModMenuTypes.ANNIHILATION_FURNACE.get(), containerId);
        this.blockEntity = blockEntity;

        // 输入物品槽（18 格，3 行 × 6 列）
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 6; col++) {
                this.addSlot(new SlotItemHandler(blockEntity.getInputInventory(), row * 6 + col, 44 + col * 18, 18 + row * 18));
            }
        }

        // 玩家背包（3 行 × 9 列）
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 104 + row * 18));
            }
        }
        // 玩家快捷栏（9 格）
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 162));
        }
    }

    /** 网络创建构造函数 */
    public AnnihilationFurnaceMenu(int containerId, Inventory playerInventory, FriendlyByteBuf extraData) {
        this(containerId, playerInventory, getBlockEntity(playerInventory, extraData));
    }

    private static AnnihilationFurnaceBlockEntity getBlockEntity(Inventory playerInventory, FriendlyByteBuf buf) {
        BlockPos pos = buf.readBlockPos();
        Level level = playerInventory.player.level();
        if (level.getBlockEntity(pos) instanceof AnnihilationFurnaceBlockEntity be) {
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
            if (index < AnnihilationFurnaceBlockEntity.INPUT_SLOTS) {
                if (!this.moveItemStackTo(stackInSlot, AnnihilationFurnaceBlockEntity.INPUT_SLOTS, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(stackInSlot, 0, AnnihilationFurnaceBlockEntity.INPUT_SLOTS, false)) {
                return ItemStack.EMPTY;
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

    public AnnihilationFurnaceBlockEntity getBlockEntity() {
        return blockEntity;
    }
}
