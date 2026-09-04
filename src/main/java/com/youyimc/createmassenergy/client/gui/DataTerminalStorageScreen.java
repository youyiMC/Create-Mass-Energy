package com.youyimc.createmassenergy.client.gui;

import java.util.List;

import com.youyimc.createmassenergy.block.entity.DataTerminalBlockEntity;
import com.youyimc.createmassenergy.block.entity.DataTerminalBlockEntity.StoredData;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

/**
 * 数据化终端 · 已存储物品只读查看器。
 * <p>
 * 纯展示用（无法交互/取物）：以物品格子网格展示当前数据化存储中每种物品，
 * 每格显示一个物品图标，悬停可查看该物品存储的总数量。
 * 打开方式：在数据化终端 GUI 中点击"查看存储"按钮。
 */
public class DataTerminalStorageScreen extends Screen {

    /** 每行格子数 */
    private static final int SLOTS_PER_ROW = 9;
    /** 格子像素尺寸 */
    private static final int SLOT = 18;
    /** 格子内容（物品图标）实际绘制尺寸 */
    private static final int ITEM_SIZE = 16;
    /** 标题栏高度 */
    private static final int HEADER_H = 18;

    /** 面板背景色（半透明） */
    private static final int BG_COLOR = 0xC0101010;
    /** 槽位底色 */
    private static final int SLOT_BG = 0x33000000;
    /** 悬停高亮 */
    private static final int HOVER_COLOR = 0x55FFFFFF;

    private final DataTerminalBlockEntity blockEntity;

    public DataTerminalStorageScreen(DataTerminalBlockEntity blockEntity) {
        super(Component.translatable("gui.createmassenergy.data_terminal.storage_title"));
        this.blockEntity = blockEntity;
    }

    @Override
    protected void init() {
        int panelH = panelHeight();
        int cx = this.width / 2;
        int topY = Math.max(20, (this.height - panelH) / 2);
        int panelW = gridWidth();
        // 关闭按钮（右上角）
        this.addRenderableWidget(Button.builder(Component.literal("✕"), b -> this.onClose())
            .bounds(cx + panelW / 2 - 12, topY - 2, 24, 16)
            .build());
    }

    private List<StoredData> storedData() {
        return blockEntity != null ? blockEntity.getStoredData() : List.of();
    }

    private int rowCount() {
        int n = storedData().size();
        return n == 0 ? 1 : (n + SLOTS_PER_ROW - 1) / SLOTS_PER_ROW;
    }

    private int gridWidth() {
        return SLOTS_PER_ROW * SLOT;
    }

    private int panelHeight() {
        return HEADER_H + rowCount() * SLOT;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        int panelW = gridWidth();
        int panelH = panelHeight();
        int cx = this.width / 2;
        int leftX = cx - panelW / 2;
        int topY = Math.max(20, (this.height - panelH) / 2);

        // 面板背景
        guiGraphics.fill(leftX - 4, topY - 4, leftX + panelW + 4, topY + panelH + 4, BG_COLOR);
        // 标题
        guiGraphics.drawString(this.font, this.title,
            leftX + 2, topY + 5, 0xFFFFFF, false);

        List<StoredData> stored = storedData();
        if (stored.isEmpty()) {
            guiGraphics.drawString(this.font,
                Component.translatable("gui.createmassenergy.data_terminal.storage_empty"),
                leftX + 4, topY + HEADER_H + 8, 0x909090, false);
            return;
        }

        // 已存储物品网格（只读，无法交互）
        int hovered = -1;
        for (int i = 0; i < stored.size(); i++) {
            StoredData data = stored.get(i);
            int col = i % SLOTS_PER_ROW;
            int row = i / SLOTS_PER_ROW;
            int slotX = leftX + col * SLOT;
            int slotY = topY + HEADER_H + row * SLOT;

            // 槽位背景
            guiGraphics.fill(slotX + 1, slotY + 1, slotX + ITEM_SIZE + 1, slotY + ITEM_SIZE + 1, SLOT_BG);

            // 悬停高亮
            if (mouseX >= slotX && mouseX < slotX + ITEM_SIZE
                && mouseY >= slotY && mouseY < slotY + ITEM_SIZE) {
                guiGraphics.fill(slotX + 1, slotY + 1, slotX + ITEM_SIZE + 1, slotY + ITEM_SIZE + 1, HOVER_COLOR);
                hovered = i;
            }

            // 物品图标（count 置为 1 展示，真实总量在 tooltip）
            ItemStack display = data.item.copy();
            display.setCount(1);
            guiGraphics.renderItem(display, slotX + 1, slotY + 1);
        }

        // 悬停 tooltip：物品名 x 总数
        if (hovered >= 0 && hovered < stored.size()) {
            StoredData data = stored.get(hovered);
            Component tip = Component.translatable(
                "gui.createmassenergy.data_terminal.storage_entry",
                data.item.getHoverName(), data.count);
            guiGraphics.renderTooltip(this.font, tip, mouseX, mouseY);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
