package com.youyimc.createmassenergy.client.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.youyimc.createmassenergy.CreateMassenergy;
import com.youyimc.createmassenergy.block.entity.RadioTelegraphBlockEntity;
import com.youyimc.createmassenergy.menu.RadioTelegraphMenu;
import com.youyimc.createmassenergy.network.RadioTelegraphModePayload;
import com.youyimc.createmassenergy.network.RadioTelegraphNamePayload;
import com.youyimc.createmassenergy.network.RadioTelegraphPairPayload;
import com.youyimc.createmassenergy.network.RadioTelegraphReceiversPayload;
import com.youyimc.createmassenergy.network.RadioTelegraphRequestReceiversPayload;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * 收发报机 GUI 屏幕。
 * <p>
 * 布局（相对 176x170 的主 GUI）：
 * <ul>
 *   <li>命名输入框 + 保存按钮 + 模式按钮：GUI 上方外侧（避免与 GUI 内部元素重叠）</li>
 *   <li>配对接收器列表（发送模式）：GUI 左侧外侧，复用 MC 原版服务器列表的
 *       信号图标（{@code server_list/ping_*} sprite）与半透明面板背景</li>
 *   <li>主 GUI 内：6 升级插槽 + 玩家背包</li>
 * </ul>
 */
public class RadioTelegraphScreen extends AbstractContainerScreen<RadioTelegraphMenu> {

    private static final ResourceLocation TEXTURE =
        ResourceLocation.fromNamespaceAndPath(CreateMassenergy.MODID, "textures/gui/radio_telegraph.png");

    // ---- 命名栏（GUI 上方外侧）----
    private static final int HEADER_H = 18;

    // ---- 配对列表（GUI 左侧外侧）----
    private static final int PAIR_LIST_WIDTH = 124;
    private static final int PAIR_LIST_HEIGHT = 148;
    private static final int PAIR_LIST_ROW_H = 15;
    private static final int PAIR_GAP = 8; // 配对列表与主 GUI 的间距

    // ---- MC 原版服务器列表信号图标 sprite ----
    private static final ResourceLocation PING_1 = ResourceLocation.withDefaultNamespace("server_list/ping_1");
    private static final ResourceLocation PING_2 = ResourceLocation.withDefaultNamespace("server_list/ping_2");
    private static final ResourceLocation PING_3 = ResourceLocation.withDefaultNamespace("server_list/ping_3");
    private static final ResourceLocation PING_4 = ResourceLocation.withDefaultNamespace("server_list/ping_4");
    private static final ResourceLocation PING_5 = ResourceLocation.withDefaultNamespace("server_list/ping_5");
    private static final ResourceLocation UNREACHABLE = ResourceLocation.withDefaultNamespace("server_list/unreachable");

    private EditBox nameBox;
    private Button modeButton;
    private Button saveNameButton;

    /** 服务端推送的可用接收器列表（发送模式配对用） */
    private List<RadioTelegraphReceiversPayload.ReceiverInfo> receivers = new ArrayList<>();

    public RadioTelegraphScreen(RadioTelegraphMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 170;
    }

    @Override
    protected void init() {
        super.init();
        int x = this.leftPos;
        int y = this.topPos;

        // ---- 命名栏 + 按钮（GUI 上方外侧）----
        // 命名输入框
        this.nameBox = new EditBox(this.font, x + 26, y - HEADER_H + 2, 96, 14, Component.literal("Name"));
        this.nameBox.setMaxLength(32);
        this.nameBox.setValue("");
        this.addRenderableWidget(nameBox);

        // 保存名字按钮
        this.saveNameButton = Button.builder(Component.literal("OK"), button -> saveName())
            .bounds(x + 126, y - HEADER_H, 22, HEADER_H - 2)
            .build();
        this.addRenderableWidget(saveNameButton);

        // 模式切换按钮（命名栏右侧）
        this.modeButton = Button.builder(Component.literal(""), button -> toggleMode())
            .bounds(x + 152, y - HEADER_H, 40, HEADER_H - 2)
            .build();
        this.addRenderableWidget(modeButton);

        // 从服务器方块实体同步名字（仅客户端显示用）
        RadioTelegraphBlockEntity be = menu.getBlockEntity();
        if (be != null) {
            this.nameBox.setValue(be.getRadioName());
        }
        updateModeButtonText();

        // 请求服务端推送可用接收器列表（发送模式才需要）
        if (be != null && be.getMode() == RadioTelegraphBlockEntity.Mode.SEND) {
            PacketDistributor.sendToServer(new RadioTelegraphRequestReceiversPayload(be.getBlockPos()));
        }
    }

    /**
     * 接收服务端推送的接收器列表（由网络包 handler 调用）。
     */
    public static void receiveReceivers(RadioTelegraphReceiversPayload payload) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen instanceof RadioTelegraphScreen screen) {
            screen.receivers = new ArrayList<>(payload.receivers());
        }
    }

    private void saveName() {
        RadioTelegraphBlockEntity be = menu.getBlockEntity();
        if (be != null) {
            PacketDistributor.sendToServer(new RadioTelegraphNamePayload(be.getBlockPos(), nameBox.getValue()));
        }
    }

    private void toggleMode() {
        RadioTelegraphBlockEntity be = menu.getBlockEntity();
        if (be != null) {
            boolean receive = be.getMode() != RadioTelegraphBlockEntity.Mode.RECEIVE;
            PacketDistributor.sendToServer(new RadioTelegraphModePayload(be.getBlockPos(), receive));
            updateModeButtonText();
            // 切到发送模式后刷新接收器列表
            if (receive) {
                PacketDistributor.sendToServer(new RadioTelegraphRequestReceiversPayload(be.getBlockPos()));
            }
        }
    }

    /** 切换某个接收器的配对状态 */
    private void togglePair(UUID receiverId) {
        RadioTelegraphBlockEntity be = menu.getBlockEntity();
        if (be != null && be.getMode() == RadioTelegraphBlockEntity.Mode.SEND) {
            PacketDistributor.sendToServer(new RadioTelegraphPairPayload(be.getBlockPos(), receiverId));
        }
    }

    private void updateModeButtonText() {
        if (modeButton == null) {
            return;
        }
        RadioTelegraphBlockEntity be = menu.getBlockEntity();
        if (be != null) {
            boolean receive = be.getMode() == RadioTelegraphBlockEntity.Mode.RECEIVE;
            modeButton.setMessage(Component.translatable(receive
                ? "gui.createmassenergy.radio_telegraph.mode.receive"
                : "gui.createmassenergy.radio_telegraph.mode.send"));
        }
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = this.leftPos;
        int y = this.topPos;
        guiGraphics.blit(TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight, this.imageWidth, this.imageHeight);

        // 命名栏背景（GUI 上方外侧半透明横条，承载命名框与按钮）
        guiGraphics.fill(x + 24, y - HEADER_H + 1, x + this.imageWidth - 8, y - 2, 0x66000000);

        // 发送模式：绘制左侧配对列表面板背景
        RadioTelegraphBlockEntity be = menu.getBlockEntity();
        if (be != null && be.getMode() == RadioTelegraphBlockEntity.Mode.SEND) {
            renderPairListPanel(guiGraphics, x, y);
        }
    }

    /** 绘制配对列表面板（半透明背景 + 标题） */
    private void renderPairListPanel(GuiGraphics guiGraphics, int x, int y) {
        int listX = x - PAIR_GAP - PAIR_LIST_WIDTH;
        int listY = y + 2;
        // 面板背景
        guiGraphics.fill(listX, listY, listX + PAIR_LIST_WIDTH, listY + PAIR_LIST_HEIGHT, 0xC0101010);
        // 标题
        guiGraphics.drawString(this.font,
            Component.translatable("gui.createmassenergy.radio_telegraph.pair_title"),
            listX + 5, listY + 4, 0xFFFFFF, false);
        // 分隔线
        guiGraphics.fill(listX, listY + 13, listX + PAIR_LIST_WIDTH, listY + 14, 0xFF555555);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        super.renderLabels(guiGraphics, mouseX, mouseY);
        guiGraphics.drawString(this.font, this.title, 8, 6, 4210752, false);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        // 发送模式：绘制配对列表行（在 GUI 外层，须在 super.render 之后绘制以免被裁剪）
        RadioTelegraphBlockEntity be = menu.getBlockEntity();
        if (be != null && be.getMode() == RadioTelegraphBlockEntity.Mode.SEND) {
            renderPairListRows(guiGraphics, mouseX, mouseY);
        }

        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    /** 绘制配对列表的每一行：信号图标 + 名字 + 配对状态 */
    private void renderPairListRows(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        RadioTelegraphBlockEntity be = menu.getBlockEntity();
        if (be == null) {
            return;
        }
        int x = this.leftPos;
        int y = this.topPos;
        int listX = x - PAIR_GAP - PAIR_LIST_WIDTH;
        int listY = y + 2;
        int startY = listY + 16;

        if (receivers.isEmpty()) {
            guiGraphics.drawString(this.font,
                Component.translatable("gui.createmassenergy.radio_telegraph.no_receivers"),
                listX + 6, startY + 3, 0x909090, false);
            return;
        }

        int maxRows = (PAIR_LIST_HEIGHT - 18) / PAIR_LIST_ROW_H;
        int drawRows = Math.min(receivers.size(), maxRows);
        List<UUID> paired = be.getPairedReceivers();

        for (int i = 0; i < drawRows; i++) {
            RadioTelegraphReceiversPayload.ReceiverInfo info = receivers.get(i);
            int rowY = startY + i * PAIR_LIST_ROW_H;
            boolean isPaired = info.paired() || paired.contains(info.id());
            boolean hover = mouseX >= listX && mouseX < listX + PAIR_LIST_WIDTH
                && mouseY >= rowY && mouseY < rowY + PAIR_LIST_ROW_H;

            // 行背景
            if (isPaired) {
                guiGraphics.fill(listX, rowY, listX + PAIR_LIST_WIDTH, rowY + PAIR_LIST_ROW_H, 0x2200AA00);
            } else if (hover) {
                guiGraphics.fill(listX, rowY, listX + PAIR_LIST_WIDTH, rowY + PAIR_LIST_ROW_H, 0x22FFFFFF);
            }

            // 信号图标（已配对 = 满格信号 ping_5，未配对 = ping_2）
            ResourceLocation icon = isPaired ? PING_5 : PING_2;
            guiGraphics.blitSprite(icon, listX + 4, rowY + 4, 10, 8);

            // 名字（截断）
            String rawName = info.name() == null || info.name().isBlank() ? "?" : info.name();
            int maxNameWidth = PAIR_LIST_WIDTH - 34;
            Component nameComp = Component.literal(rawName);
            if (this.font.width(nameComp) > maxNameWidth) {
                nameComp = Component.literal(this.font.plainSubstrByWidth(rawName, maxNameWidth - 3) + "...");
            }
            guiGraphics.drawString(this.font, nameComp, listX + 18, rowY + 3,
                isPaired ? 0x55FF55 : 0xFFFFFF, false);

            // 已配对勾选标记（右侧）
            if (isPaired) {
                guiGraphics.drawString(this.font, "\u2714", listX + PAIR_LIST_WIDTH - 11, rowY + 3, 0x55FF55, false);
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        RadioTelegraphBlockEntity be = menu.getBlockEntity();
        if (be != null && be.getMode() == RadioTelegraphBlockEntity.Mode.SEND && button == 0) {
            int idx = hitPairRow((int) mouseX, (int) mouseY);
            if (idx >= 0 && idx < receivers.size()) {
                togglePair(receivers.get(idx).id());
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    /** 检测命中的配对列表行索引；未命中返回 -1 */
    private int hitPairRow(int mouseX, int mouseY) {
        int x = this.leftPos;
        int y = this.topPos;
        int listX = x - PAIR_GAP - PAIR_LIST_WIDTH;
        int listY = y + 2;
        int startY = listY + 16;
        if (mouseX < listX || mouseX >= listX + PAIR_LIST_WIDTH || mouseY < startY) {
            return -1;
        }
        int idx = (mouseY - startY) / PAIR_LIST_ROW_H;
        int maxRows = (PAIR_LIST_HEIGHT - 18) / PAIR_LIST_ROW_H;
        if (idx >= maxRows || idx >= receivers.size()) {
            return -1;
        }
        return idx;
    }
}
