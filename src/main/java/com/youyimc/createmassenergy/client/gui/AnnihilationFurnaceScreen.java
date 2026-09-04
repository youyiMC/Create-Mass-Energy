package com.youyimc.createmassenergy.client.gui;

import com.youyimc.createmassenergy.CreateMassenergy;
import com.youyimc.createmassenergy.block.entity.AnnihilationFurnaceBlockEntity;
import com.youyimc.createmassenergy.menu.AnnihilationFurnaceMenu;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/**
 * 物品湮灭炉 GUI 屏幕
 */
public class AnnihilationFurnaceScreen extends AbstractContainerScreen<AnnihilationFurnaceMenu> {

    private static final ResourceLocation TEXTURE =
        ResourceLocation.fromNamespaceAndPath(CreateMassenergy.MODID, "textures/gui/annihilation_furnace.png");

    public AnnihilationFurnaceScreen(AnnihilationFurnaceMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 184;
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;
        guiGraphics.blit(TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight, this.imageWidth, this.imageHeight);

        AnnihilationFurnaceBlockEntity be = menu.getBlockEntity();
        if (be == null) {
            return;
        }
        // 绘制能量条（从下往上，颜色由 FE/容量 决定）
        int energy = be.getEnergy().getEnergyStored();
        int capacity = be.getEnergy().getMaxEnergyStored();
        if (capacity > 0) {
            int height = (int) (energy * 50.0 / capacity);
            int color = 0xFFE0B000;
            guiGraphics.fill(x + 8, y + 18 + (50 - height), x + 14, y + 68, color);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        super.renderLabels(guiGraphics, mouseX, mouseY);
        // 标题
        guiGraphics.drawString(this.font, this.title, 8, 6, 4210752, false);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }
}
