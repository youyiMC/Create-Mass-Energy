package com.youyimc.createmassenergy.client.gui;

import com.youyimc.createmassenergy.CreateMassenergy;
import com.youyimc.createmassenergy.block.entity.DataTerminalBlockEntity;
import com.youyimc.createmassenergy.data.DataStorage;
import com.youyimc.createmassenergy.menu.DataTerminalMenu;
import com.youyimc.createmassenergy.network.DataTerminalModePayload;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * 数据化终端 GUI 屏幕
 * 包含：模式切换按钮、能量显示、存储用量显示、物品栏、升级插槽
 */
public class DataTerminalScreen extends AbstractContainerScreen<DataTerminalMenu> {

    private static final ResourceLocation TEXTURE =
        ResourceLocation.fromNamespaceAndPath(CreateMassenergy.MODID, "textures/gui/data_terminal.png");

    private Button modeButton;
    private Button storageButton;

    public DataTerminalScreen(DataTerminalMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 178;
    }

    @Override
    protected void init() {
        super.init();
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;
        // 模式切换按钮（位于 GUI 顶部右侧）
        this.modeButton = Button.builder(Component.literal(""), button -> toggleMode())
            .bounds(x + 116, y + 20, 52, 14)
            .build();
        this.addRenderableWidget(modeButton);
        updateModeButtonText();

        // 查看存储按钮（升级槽下方、玩家栏上方的空隙）
        this.storageButton = Button.builder(
                Component.translatable("gui.createmassenergy.data_terminal.storage_btn"),
                button -> openStorageViewer())
            .bounds(x + 128, y + 78, 42, 14)
            .build();
        this.addRenderableWidget(storageButton);
    }

    /** 打开已存储物品只读查看器 */
    private void openStorageViewer() {
        DataTerminalBlockEntity be = menu.getBlockEntity();
        if (be != null) {
            this.minecraft.setScreen(new DataTerminalStorageScreen(be));
        }
    }

    private void toggleMode() {
        DataTerminalBlockEntity be = menu.getBlockEntity();
        if (be != null) {
            boolean restore = be.getMode() != DataTerminalBlockEntity.Mode.RESTORE;
            PacketDistributor.sendToServer(new DataTerminalModePayload(be.getBlockPos(), restore));
            // 本地立即更新 UI 反馈
            updateModeButtonText();
        }
    }

    private void updateModeButtonText() {
        if (modeButton == null) {
            return;
        }
        DataTerminalBlockEntity be = menu.getBlockEntity();
        if (be != null) {
            boolean restore = be.getMode() == DataTerminalBlockEntity.Mode.RESTORE;
            modeButton.setMessage(Component.translatable(restore
                ? "gui.createmassenergy.data_terminal.mode.restore"
                : "gui.createmassenergy.data_terminal.mode.decompose"));
        }
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;
        guiGraphics.blit(TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight, this.imageWidth, this.imageHeight);

        DataTerminalBlockEntity be = menu.getBlockEntity();
        if (be == null) {
            return;
        }
        // 能量条（右侧垂直）
        int energy = be.getEnergy().getEnergyStored();
        int capacity = be.getEnergy().getMaxEnergyStored();
        if (capacity > 0) {
            int height = (int) (energy * 48.0 / capacity);
            guiGraphics.fill(x + 162, y + 18 + (48 - height), x + 168, y + 66, 0xFFE0B000);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        super.renderLabels(guiGraphics, mouseX, mouseY);
        guiGraphics.drawString(this.font, this.title, 8, 6, 4210752, false);

        DataTerminalBlockEntity be = menu.getBlockEntity();
        if (be == null) {
            return;
        }
        // 存储用量文字（GUI 底部信息栏）
        long used = be.getUsedStorageBytes();
        long total = be.getTotalStorageBytes();
        String storageText = "Storage: " + DataStorage.formatBytes(used) + " / " + DataStorage.formatBytes(total);
        guiGraphics.drawString(this.font, Component.literal(storageText), 8, 82, 4210752, false);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
        updateModeButtonText();
    }
}
