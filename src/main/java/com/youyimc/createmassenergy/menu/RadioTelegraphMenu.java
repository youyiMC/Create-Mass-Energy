package com.youyimc.createmassenergy.menu;

import com.youyimc.createmassenergy.ModMenuTypes;
import com.youyimc.createmassenergy.block.entity.RadioTelegraphBlockEntity;
import com.youyimc.createmassenergy.upgrade.UpgradeSlot;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * 收发报机 GUI
 * 显示：命名、模式切换、配对列表、升级插槽
 */
public class RadioTelegraphMenu extends AbstractContainerMenu {

    private final RadioTelegraphBlockEntity blockEntity;

    public RadioTelegraphMenu(int containerId, Inventory playerInventory, RadioTelegraphBlockEntity blockEntity) {
        super(ModMenuTypes.RADIO_TELEGRAPH.get(), containerId);
        this.blockEntity = blockEntity;

        // 6 升级插槽（仅线程模块，居中：6*18=108，(176-108)/2=34）
        for (int col = 0; col < 6; col++) {
            this.addSlot(new UpgradeSlot(blockEntity, blockEntity.getUpgradeSlots(), col, 34 + col * 18, 26));
        }

        // 玩家背包（3 行 × 9 列）
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new net.minecraft.world.inventory.Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 88 + row * 18));
            }
        }
        // 玩家快捷栏（9 格）
        for (int col = 0; col < 9; col++) {
            this.addSlot(new net.minecraft.world.inventory.Slot(playerInventory, col, 8 + col * 18, 146));
        }
    }

    /** 网络创建构造函数 */
    public RadioTelegraphMenu(int containerId, Inventory playerInventory, FriendlyByteBuf extraData) {
        this(containerId, playerInventory, getBlockEntity(playerInventory, extraData));
    }

    private static RadioTelegraphBlockEntity getBlockEntity(Inventory playerInventory, FriendlyByteBuf buf) {
        BlockPos pos = buf.readBlockPos();
        Level level = playerInventory.player.level();
        if (level.getBlockEntity(pos) instanceof RadioTelegraphBlockEntity be) {
            return be;
        }
        return null;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        if (blockEntity == null) {
            return false;
        }
        return blockEntity.getLevel() != null
            && blockEntity.getBlockPos().closerToCenterThan(player.position(), 8.0);
    }

    public RadioTelegraphBlockEntity getBlockEntity() {
        return blockEntity;
    }
}
