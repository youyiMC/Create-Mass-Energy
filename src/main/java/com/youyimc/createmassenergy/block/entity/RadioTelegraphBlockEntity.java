package com.youyimc.createmassenergy.block.entity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.youyimc.createmassenergy.ModBlockEntities;
import com.youyimc.createmassenergy.block.RadioTelegraphBlock;
import com.youyimc.createmassenergy.menu.RadioTelegraphMenu;
import com.youyimc.createmassenergy.radio.RadioRegistry;
import com.youyimc.createmassenergy.thread.ThreadManager;
import com.youyimc.createmassenergy.upgrade.UpgradeContainer;
import com.youyimc.createmassenergy.upgrade.UpgradeSlotHandler;
import com.youyimc.createmassenergy.upgrade.UpgradeType;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

/**
 * 收发报机方块实体
 * <p>
 * 发送模式：从下方数据化终端读取物品数据，按优先级分配给配对接收器。
 * 接收模式：接收其他发报机发来的数据，写入下方数据化终端。
 * 特性：本身没有任何数据存储空间，不能加装存储类升级模块。
 */
public class RadioTelegraphBlockEntity extends BlockEntity implements UpgradeContainer, MenuProvider {

    /** 发送/接收模式 */
    public enum Mode {
        SEND,
        RECEIVE
    }

    private static final int BASE_SPEED_PER_SECOND = 5;

    /** 一次成功收发后保持"传输中"状态的 tick 数（2 秒，覆盖 0.5s 调度间隙） */
    private static final int ACTIVE_TICKS = 40;

    /** 收发报机唯一 ID（用于全服注册） */
    private UUID radioId = UUID.randomUUID();

    /** 当前模式 */
    private Mode mode = Mode.SEND;

    /** 名称（未命名无法工作） */
    private String name = "";

    /** 发送模式下配对的接收器 ID 列表（按优先级顺序） */
    private final List<UUID> pairedReceivers = new ArrayList<>();

    /** 升级插槽（仅线程，不能装存储） */
    private final UpgradeSlotHandler upgradeSlots = new UpgradeSlotHandler(
        Set.of(UpgradeType.THREAD), // 收发报机无存储，只能装线程
        () -> {
            setChanged();
            updateThreads();
            checkUpgradeAchievements();
        });

    /** 线程管理器（发送/接收的传输速率由线程数决定） */
    private final ThreadManager threadManager = new ThreadManager((item, count) -> {
        // 每个作业 tick 推进时执行传输
    });

    /** 累计的未发送数据量（小数部分），保证小批量数据也能按速率平滑发送 */
    private double sendAccumulator = 0.0;

    /**
     * 剩余"真正在收发数据"的活跃 tick 数。
     * 每次实际发送/接收成功时重置；为 0 表示空闲（可播待机音）。
     * WORKING 方块状态据此设置，并同步给客户端供音效判断。
     */
    private int activeTicks = 0;

    private int cooldown = 0;

    public RadioTelegraphBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.RADIO_TELEGRAPH.get(), pos, state);
    }

    // ==================== 服务器 tick ====================
    public static void serverTick(Level level, BlockPos pos, BlockState state, RadioTelegraphBlockEntity be) {
        be.tick();
    }

    private void tick() {
        if (level == null || level.isClientSide || !(level instanceof ServerLevel serverLevel)) {
            return;
        }
        // 无名称不工作
        if (name == null || name.isBlank()) {
            if (activeTicks > 0) {
                activeTicks = 0;
                updateWorkingState(false);
            }
            return;
        }
        // 活跃计时递减（每游戏 tick 一次）
        if (activeTicks > 0) {
            activeTicks--;
        }
        cooldown--;
        if (cooldown > 0) {
            updateWorkingState(activeTicks > 0);
            return;
        }
        cooldown = 10; // 每 0.5 秒调度一次

        // 向全服注册表同步状态（保存 BE 弱引用，支持航空学 sub-level 跨结构传输）
        RadioRegistry.register(radioId, serverLevel, getBlockPos(), name, mode == Mode.SEND, this);

        if (mode == Mode.SEND) {
            doSend(serverLevel);
        } else {
            doReceive(serverLevel);
        }

        // 更新工作状态（仅真正传输中才算 working）
        updateWorkingState(activeTicks > 0);
    }

    /**
     * 同步工作状态到方块状态（变化时更新方块并主动推送客户端）。
     */
    private void updateWorkingState(boolean working) {
        if (working != getBlockState().getValue(RadioTelegraphBlock.WORKING)) {
            level.setBlock(getBlockPos(), getBlockState().setValue(RadioTelegraphBlock.WORKING, working), 3);
            if (level instanceof ServerLevel sl) {
                sl.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
            }
        }
    }

    /**
     * 发送模式：从下方终端读取数据，分配给配对接收器
     */
    private void doSend(ServerLevel serverLevel) {
        DataTerminalBlockEntity terminal = getBelowTerminal(serverLevel);
        if (terminal == null || terminal.getStoredData().isEmpty()) {
            threadManager.cleanup(List.of());
            sendAccumulator = 0.0;
            return;
        }

        // 每次调度（0.5s）的累计可发送量 = 线程数 × 每秒基础速率 × 0.5 秒
        // 用 double 累积，避免整数除法在小批量/低线程数时被吞掉，使多线程对小批量数据也线性生效
        double perDispatch = getTotalThreads() * (double) BASE_SPEED_PER_SECOND * 0.5;
        sendAccumulator += perDispatch;
        int quota = (int) sendAccumulator;
        sendAccumulator -= quota;
        if (quota <= 0) {
            // 即使速率极低也至少保证每个调度尝试发送（若配额累计仍为 0，则跳过本调度）
            return;
        }

        // 按优先级分配：并列的接收器平均分配，奇数优先给靠左的
        List<UUID> targets = resolvePriorityTargets(serverLevel);
        if (targets.isEmpty()) {
            return;
        }

        // 为每种数据建立作业并推进（简化为直接按速率发送）
        for (DataTerminalBlockEntity.StoredData data : new ArrayList<>(terminal.getStoredData())) {
            if (data.count <= 0) {
                continue;
            }
            // 本调度最多发送 quota 个（配额不足时留给下个调度）
            int toSend = (int) Math.min(quota, Math.min(data.count, Integer.MAX_VALUE));
            if (toSend <= 0) {
                continue;
            }
            quota -= toSend;
            // 平均分配给目标接收器
            int perTarget = toSend / targets.size();
            int remainder = toSend % targets.size();
            for (int i = 0; i < targets.size(); i++) {
                int amount = perTarget + (i < remainder ? 1 : 0);
                if (amount <= 0) {
                    continue;
                }
                RadioRegistry.Entry receiverEntry = RadioRegistry.get(targets.get(i));
                if (receiverEntry == null || receiverEntry.level == null || receiverEntry.level.isClientSide) {
                    continue;
                }
                // 发送到接收器下方的终端（使用弱引用获取 BE，跨 sub-level 有效）
                BlockEntity be = receiverEntry.getBlockEntity();
                if (be instanceof RadioTelegraphBlockEntity receiver) {
                    if (receiver.receiveData(data.item, amount)) {
                        data.count -= amount;
                        // 实际发送成功：标记活跃（供工作音效/状态同步）
                        activeTicks = ACTIVE_TICKS;
                    }
                }
            }
            if (data.count <= 0) {
                terminal.getStoredData().remove(data);
            }
            if (quota <= 0) {
                break; // 本调度配额已用完
            }
        }
        terminal.setChanged();
    }

    /**
     * 接收模式：接收数据并写入下方终端
     */
    private boolean receiveData(ItemStack item, int count) {
        DataTerminalBlockEntity terminal = getBelowTerminal(level);
        if (terminal == null) {
            return false;
        }
        // 检查存储容量
        long bytes = com.youyimc.createmassenergy.data.DataStorage.bytesFor(item, count);
        if (bytes > terminal.getRemainingStorageBytes()) {
            return false;
        }
        // 写入存储
        for (DataTerminalBlockEntity.StoredData data : terminal.getStoredData()) {
            if (ItemStack.isSameItemSameComponents(data.item, item)) {
                data.count += count;
                terminal.setChanged();
                triggerMarconiEra();
                // 实际接收成功：标记接收方活跃（供工作音效/状态同步）
                activeTicks = ACTIVE_TICKS;
                return true;
            }
        }
        terminal.getStoredData().add(new DataTerminalBlockEntity.StoredData(item, count));
        terminal.setChanged();
        triggerMarconiEra();
        // 实际接收成功：标记接收方活跃（供工作音效/状态同步）
        activeTicks = ACTIVE_TICKS;
        return true;
    }

    /** 马可尼时代：16格内收发器接收或发送物品数据一次 */
    private void triggerMarconiEra() {
        if (level == null || level.isClientSide) {
            return;
        }
        var player = level.getNearestPlayer(getBlockPos().getX(), getBlockPos().getY(), getBlockPos().getZ(), 16.0, false);
        if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
            serverPlayer.getAdvancements().award(serverPlayer.server.getAdvancements().get(
                com.youyimc.createmassenergy.CreateMassenergy.rl("marconi_era")), "trigger");
        }
    }

    private void doReceive(ServerLevel serverLevel) {
        // 接收模式：本身不主动拉取，等待发报机推送
        // 这里仅用于工作状态显示（当有下游终端且有存储时视为活跃）
        DataTerminalBlockEntity terminal = getBelowTerminal(serverLevel);
        if (terminal != null && !terminal.getStoredData().isEmpty()) {
            threadManager.ensureJobFor(ItemStack.EMPTY);
        }
        threadManager.cleanup(List.of());
    }

    /**
     * 获取下方数据化终端
     */
    private DataTerminalBlockEntity getBelowTerminal(Level world) {
        if (world == null) {
            return null;
        }
        BlockEntity be = world.getBlockEntity(getBlockPos().below());
        return be instanceof DataTerminalBlockEntity ? (DataTerminalBlockEntity) be : null;
    }

    /**
     * 解析优先级分配目标。
     * 按配对顺序（优先级从高到低），并列的接收器平均分配。
     * 简化：直接按配对列表顺序分配，并列的（同一优先级）平均分配。
     */
    private List<UUID> resolvePriorityTargets(ServerLevel serverLevel) {
        List<UUID> result = new ArrayList<>();
        for (UUID id : pairedReceivers) {
            RadioRegistry.Entry entry = RadioRegistry.get(id);
            if (entry != null && !entry.isSender && entry.level != null && !entry.level.isClientSide
                && entry.name != null && !entry.name.isBlank()) {
                result.add(id);
            }
        }
        return result;
    }

    // ==================== 访问器 ====================

    /** 是否正在传输数据（读方块状态；客户端/服务器均可用） */
    public boolean isWorking() {
        return getBlockState().getValue(RadioTelegraphBlock.WORKING);
    }

    public Mode getMode() {
        return mode;
    }

    public void setMode(Mode mode) {
        if (this.mode == mode) {
            return;
        }
        this.mode = mode;
        setChanged();
        syncToClient();
    }

    public String getRadioName() {
        return name;
    }

    public void setRadioName(String name) {
        String newName = name == null ? "" : name;
        if (this.name.equals(newName)) {
            return;
        }
        this.name = newName;
        setChanged();
        syncToClient();
    }

    /**
     * 向客户端推送方块实体更新（模式、名称等需要客户端同步的状态）。
     */
    public void syncToClient() {
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
        }
    }

    public UUID getRadioId() {
        return radioId;
    }

    public List<UUID> getPairedReceivers() {
        return pairedReceivers;
    }

    public void setPairedReceivers(List<UUID> pairedReceivers) {
        this.pairedReceivers.clear();
        this.pairedReceivers.addAll(pairedReceivers);
        setChanged();
    }

    public void togglePair(UUID receiverId) {
        if (pairedReceivers.contains(receiverId)) {
            pairedReceivers.remove(receiverId);
        } else {
            pairedReceivers.add(receiverId);
        }
        setChanged();
        syncToClient();
    }

    public UpgradeSlotHandler getUpgradeSlots() {
        return upgradeSlots;
    }

    public ThreadManager getThreadManager() {
        return threadManager;
    }

    // ==================== 菜单提供 ====================

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.createmassenergy.radio_telegraph");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new RadioTelegraphMenu(containerId, inventory, this);
    }

    // ==================== 升级插槽 ====================

    @Override
    public ItemStack getUpgradeStack(int slot) {
        return upgradeSlots.getUpgradeStack(slot);
    }

    @Override
    public void setUpgradeStack(int slot, ItemStack stack) {
        upgradeSlots.setUpgradeStack(slot, stack);
    }

    @Override
    public void setUpgradeChanged() {
        upgradeSlots.setUpgradeChanged();
    }

    @Override
    public Set<UpgradeType> getAllowedUpgradeTypes() {
        return Set.of(UpgradeType.THREAD);
    }

    private void updateThreads() {
        threadManager.setAvailableThreads(getTotalThreads());
        threadManager.onChanged();
        setChanged();
    }

    /**
     * 检查并触发升级相关成就
     */
    private void checkUpgradeAchievements() {
        if (level == null || level.isClientSide) {
            return;
        }
        var player = level.getNearestPlayer(getBlockPos().getX(), getBlockPos().getY(), getBlockPos().getZ(), 8.0, false);
        if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
            com.youyimc.createmassenergy.advancement.AdvancementHelper.checkUpgradeAchievements(this, serverPlayer);
        }
    }

    // ==================== 能力注册 ====================

    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        // 收发报机没有物品/能量能力
    }

    // ==================== 数据保存 ====================

    @Override
    public void setRemoved() {
        // 从全服注册表移除
        if (level != null && !level.isClientSide) {
            RadioRegistry.unregister(radioId);
        }
        super.setRemoved();
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putUUID("RadioId", radioId);
        tag.putString("Mode", mode.name());
        tag.putString("Name", name);
        tag.put("UpgradeSlots", upgradeSlots.serializeNBT(registries));
        // 配对接收器
        ListTag pairList = new ListTag();
        for (UUID id : pairedReceivers) {
            pairList.add(StringTag.valueOf(id.toString()));
        }
        tag.put("PairedReceivers", pairList);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.hasUUID("RadioId")) {
            radioId = tag.getUUID("RadioId");
        }
        if (tag.contains("Mode")) {
            try {
                mode = Mode.valueOf(tag.getString("Mode"));
            } catch (IllegalArgumentException ignored) {
                mode = Mode.SEND;
            }
        }
        name = tag.getString("Name");
        upgradeSlots.deserializeNBT(registries, tag.getCompound("UpgradeSlots"));
        pairedReceivers.clear();
        ListTag pairList = tag.getList("PairedReceivers", 8);
        for (int i = 0; i < pairList.size(); i++) {
            try {
                pairedReceivers.add(UUID.fromString(pairList.getString(i)));
            } catch (IllegalArgumentException ignored) {
            }
        }
        updateThreads();
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        saveAdditional(tag, registries);
        return tag;
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
