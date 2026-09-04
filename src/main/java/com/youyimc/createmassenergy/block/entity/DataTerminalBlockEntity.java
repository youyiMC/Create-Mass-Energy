package com.youyimc.createmassenergy.block.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.youyimc.createmassenergy.ModBlockEntities;
import com.youyimc.createmassenergy.block.DataTerminalBlock;
import com.youyimc.createmassenergy.data.DataStorage;
import com.youyimc.createmassenergy.energy.SimpleEnergyStorage;
import com.youyimc.createmassenergy.menu.DataTerminalMenu;
import com.youyimc.createmassenergy.thread.ThreadManager;
import com.youyimc.createmassenergy.upgrade.UpgradeContainer;
import com.youyimc.createmassenergy.upgrade.UpgradeSlotHandler;
import com.youyimc.createmassenergy.upgrade.UpgradeType;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

/**
 * 数据化终端方块实体
 * <p>
 * 分解模式：物品 → 数据（隐藏存储）+ FE（产出 200 FE/物品，50% 损耗）
 * 还原模式：数据 → 物品（消耗 400 FE/物品）
 * 10kFE 能量缓冲；64MB 基础存储 + 存储升级叠加；8 格物品栏；6 升级插槽。
 */
public class DataTerminalBlockEntity extends BlockEntity implements UpgradeContainer, MenuProvider {

    /** 物品栏槽位数 */
    public static final int INVENTORY_SLOTS = 8;
    /** 能量缓冲容量 (FE) */
    public static final int ENERGY_CAPACITY = 10_000;
    /** 每 tick 最大能量接收 */
    public static final int MAX_RECEIVE = 2_000;
    /** 每 tick 最大能量输出 */
    public static final int MAX_OUTPUT = 4_000;
    /** 基础存储容量：64MB */
    public static final long BASE_STORAGE_BYTES = 64L * 1024L * 1024L;
    /** 基础处理速度：5 物品/秒/线程 */
    public static final double BASE_SPEED_PER_TICK = 5.0 / 20.0;
    /** 质能方程常数 t */
    public static final int ENERGY_CONSTANT = 20;
    /** 分解模式能量损耗比例（产出 = 理论 × (1-0.5) = 200 FE/物品） */
    public static final double DECOMPOSE_LOSS_RATIO = 0.5;
    /** 一次成功处理后保持"工作中"状态的 tick 数（1.5 秒，覆盖 0.5s 调度间隙） */
    public static final int ACTIVE_TICKS = 30;

    /** 工作模式 */
    public enum Mode {
        DECOMPOSE, // 数据化分解
        RESTORE    // 还原
    }

    /** 当前工作模式 */
    private Mode mode = Mode.DECOMPOSE;

    /** 8 格物品栏（物理存储，可被漏斗输入/输出） */
    private final ItemStackHandler inventory = new ItemStackHandler(INVENTORY_SLOTS) {
        @Override
        protected void onContentsChanged(int slot) {
            super.onContentsChanged(slot);
            setChanged();
            updateJobs();
        }
    };

    /** 升级插槽（线程 + 存储） */
    private final UpgradeSlotHandler upgradeSlots = new UpgradeSlotHandler(
        Set.of(UpgradeType.THREAD, UpgradeType.STORAGE),
        () -> {
            setChanged();
            updateThreads();
            checkUpgradeAchievements();
        });

    /** 能量缓冲（接收 + 输出） */
    private final SimpleEnergyStorage energy = new SimpleEnergyStorage(ENERGY_CAPACITY, MAX_RECEIVE, MAX_OUTPUT) {
        @Override
        protected void onEnergyChanged() {
            setChanged();
        }
    };

    /** 线程管理器 */
    private final ThreadManager threadManager = new ThreadManager((item, count) -> {
        if (mode == Mode.DECOMPOSE) {
            decomposeItem(item, count);
        } else {
            restoreItem(item, count);
        }
    });

    // ==================== 数据化存储（"假存储"，实际是隐藏物品栏） ====================
    /**
     * 文档规定：存储、数据什么的都是假的，实际上只是把物品放在了玩家看不到的后台。
     * 这里用隐藏物品栏实现，容量上限为总存储字节数换算出的物品数。
     */
    private final List<StoredData> storedData = new ArrayList<>();

    /** 已存储数据条目 */
    public static class StoredData {
        public final ItemStack item;
        public long count;

        public StoredData(ItemStack item, long count) {
            this.item = item.copy();
            this.item.setCount(1);
            this.count = count;
        }
    }

    private int cooldown = 0;

    /**
     * 剩余"真正处理中"的活跃 tick 数。
     * <p>
     * 每当实际分解/还原成功（不是因能量不足/存储满/物品栏满而暂停）时被重置，
     * 逐游戏 tick 递减。为 0 表示机器空闲或暂停，此时不播放工作音效。
     * 这样硬盘类音效只在实际读写时响起，暂停时"停转"。
     */
    private int activeTicks = 0;

    public DataTerminalBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.DATA_TERMINAL.get(), pos, state);
    }

    // ==================== 服务器 tick ====================
    public static void serverTick(Level level, BlockPos pos, BlockState state, DataTerminalBlockEntity be) {
        be.tick();
    }

    private void tick() {
        if (level == null || level.isClientSide) {
            return;
        }
        // 活跃计时递减（每游戏 tick 一次）
        if (activeTicks > 0) {
            activeTicks--;
        }
        cooldown--;
        if (cooldown > 0) {
            // 非调度帧也需同步 WORKING 状态变化（活跃结束时应停止音效/动画）
            updateWorkingState(activeTicks > 0);
            return;
        }
        cooldown = 10; // 每 0.5 秒调度一次

        updateJobs();
        // 推进处理；成功处理会在回调中重置 activeTicks
        threadManager.tick();
        // 向相邻方块输出多余能量，避免满能后卡住
        pushEnergy();

        // 更新工作状态（渲染）：仅"真正处理中"才算工作
        updateWorkingState(activeTicks > 0);

        // 周期性向客户端同步（存储数据/能量/模式），供存储查看器等展示最新状态
        syncToClient();
    }

    /**
     * 同步工作状态到方块状态（变化时更新方块并主动推送客户端）。
     */
    private void updateWorkingState(boolean working) {
        if (working != getBlockState().getValue(DataTerminalBlock.WORKING)) {
            level.setBlock(getBlockPos(), getBlockState().setValue(DataTerminalBlock.WORKING, working), 3);
            // 主动向客户端推送，使工作音效/动画及时停止或启动
            if (level instanceof net.minecraft.server.level.ServerLevel sl) {
                sl.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
            }
        }
    }

    // ==================== 作业调度 ====================

    /**
     * 向相邻方块输出多余能量，防止能量缓冲满后机器卡住。
     */
    private void pushEnergy() {
        if (energy.getEnergyStored() <= 0 || level == null) {
            return;
        }
        for (Direction direction : Direction.values()) {
            BlockPos neighbor = getBlockPos().relative(direction);
            var cap = level.getCapability(net.neoforged.neoforge.capabilities.Capabilities.EnergyStorage.BLOCK,
                    neighbor, direction.getOpposite());
            if (cap != null && cap.canReceive()) {
                int transferred = cap.receiveEnergy(energy.extractEnergy(energy.getEnergyStored(), true), false);
                if (transferred > 0) {
                    energy.consumeEnergy(transferred);
                }
            }
        }
    }

    private void updateJobs() {
        if (mode == Mode.DECOMPOSE) {
            // 分解模式：为物品栏中的每种物品建立作业
            boolean hasItem = false;
            for (int i = 0; i < inventory.getSlots(); i++) {
                ItemStack stack = inventory.getStackInSlot(i);
                if (!stack.isEmpty()) {
                    hasItem = true;
                    threadManager.ensureJobFor(stack);
                }
            }
            if (!hasItem) {
                threadManager.cleanup(List.of());
            }
        } else {
            // 还原模式：为已存储的每种数据建立作业
            List<ItemStack> available = new ArrayList<>();
            for (StoredData data : storedData) {
                if (data.count > 0) {
                    available.add(data.item);
                }
            }
            if (available.isEmpty()) {
                threadManager.cleanup(List.of());
            } else {
                for (ItemStack stack : available) {
                    threadManager.ensureJobFor(stack);
                }
            }
        }
    }

    // ==================== 分解逻辑 ====================
    private void decomposeItem(ItemStack item, int count) {
        // 检查能量缓冲：满则停止分解
        if (energy.getEnergyStored() >= energy.getMaxEnergyStored()) {
            return;
        }
        // 统计物理栏中可匹配的同种物品总数（扣多少就存多少，避免"先扣后存"导致物品蒸发）
        int available = 0;
        for (int i = 0; i < inventory.getSlots(); i++) {
            ItemStack stack = inventory.getStackInSlot(i);
            if (!stack.isEmpty() && ItemStack.isSameItemSameComponents(stack, item)) {
                available += stack.getCount();
            }
        }
        // 本调度实际能处理的数量 = min(Job 要求的处理量, 栏内实际可扣数量)
        int toProcess = Math.min(count, available);
        if (toProcess <= 0) {
            return; // 栏内已无此物品，等待补充
        }
        // 容量检查按实际处理量计算；不足则一个都不扣（物品保留在物理栏，不丢失）
        long bytes = DataStorage.bytesFor(item, toProcess);
        if (bytes > getRemainingStorageBytes()) {
            return;
        }
        // 从物理物品栏扣除 toProcess 个
        int remaining = toProcess;
        for (int i = 0; i < inventory.getSlots() && remaining > 0; i++) {
            ItemStack stack = inventory.getStackInSlot(i);
            if (!stack.isEmpty() && ItemStack.isSameItemSameComponents(stack, item)) {
                int toRemove = Math.min(remaining, stack.getCount());
                stack.shrink(toRemove);
                remaining -= toRemove;
                if (stack.isEmpty()) {
                    inventory.setStackInSlot(i, ItemStack.EMPTY);
                } else {
                    inventory.setStackInSlot(i, stack);
                }
            }
        }
        if (remaining > 0) {
            // 理论上不会发生（已按栏内实际数量扣），防御
            return;
        }
        addStored(item, toProcess);
        // 产出 FE：E = q·t² × (1-0.5)，每物品 200 FE
        long theoretical = (long) toProcess * ENERGY_CONSTANT * ENERGY_CONSTANT;
        int actual = (int) (theoretical * (1.0 - DECOMPOSE_LOSS_RATIO));
        energy.addEnergy(actual);
        // 实际处理成功：标记活跃（供工作音效/动画使用）
        activeTicks = ACTIVE_TICKS;
        setChanged();
    }

    // ==================== 还原逻辑 ====================
    private void restoreItem(ItemStack item, int count) {
        // 从存储中查找数据
        StoredData data = findStored(item);
        if (data == null || data.count <= 0) {
            return; // 无此数据
        }
        // 实际还原数量 = min(Job 要求的处理量, 存储中的实际数量)
        // 不能因为 Job 的速率(count)超过库存就整体卡住；改为每次还原能还原的量
        int toRestore = (int) Math.min(count, data.count);
        if (toRestore <= 0) {
            return;
        }
        // 需要足够的能量：每物品 400 FE（按实际还原量）
        long needed = (long) toRestore * ENERGY_CONSTANT * ENERGY_CONSTANT;
        if (energy.getEnergyStored() < needed) {
            return; // 能量不足，等待积累
        }
        // 尝试放入物品栏（先尝试合并到已有同类物品，再放入空槽）
        int remaining = toRestore;
        for (int i = 0; i < inventory.getSlots() && remaining > 0; i++) {
            ItemStack slot = inventory.getStackInSlot(i);
            if (slot.isEmpty()) {
                ItemStack toAdd = item.copy();
                int toAddCount = Math.min(remaining, toAdd.getMaxStackSize());
                toAdd.setCount(toAddCount);
                inventory.setStackInSlot(i, toAdd);
                remaining -= toAddCount;
            } else if (ItemStack.isSameItemSameComponents(slot, item) && slot.getCount() < slot.getMaxStackSize()) {
                int toAddCount = Math.min(remaining, slot.getMaxStackSize() - slot.getCount());
                slot.grow(toAddCount);
                inventory.setStackInSlot(i, slot);
                remaining -= toAddCount;
            }
        }
        if (remaining > 0) {
            return; // 物品栏已满，等待空间（未扣数据，安全）
        }
        // 扣减数据与能量
        data.count -= toRestore;
        if (data.count <= 0) {
            storedData.remove(data);
        }
        energy.consumeEnergy((int) needed);
        // 实际还原成功：标记活跃（供工作音效/动画使用）
        activeTicks = ACTIVE_TICKS;
        setChanged();
    }

    // ==================== 数据存储管理 ====================

    private void addStored(ItemStack item, long count) {
        StoredData data = findStored(item);
        if (data != null) {
            data.count += count;
        } else {
            storedData.add(new StoredData(item, count));
        }
        setChanged();
    }

    private StoredData findStored(ItemStack item) {
        for (StoredData data : storedData) {
            if (ItemStack.isSameItemSameComponents(data.item, item)) {
                return data;
            }
        }
        return null;
    }

    /**
     * 获取已存储的数据量（字节）
     */
    public long getUsedStorageBytes() {
        long total = 0;
        for (StoredData data : storedData) {
            total += DataStorage.bytesFor(data.item, data.count);
        }
        return total;
    }

    /**
     * 获取总存储容量（基础 64MB + 存储升级叠加）
     */
    public long getTotalStorageBytes() {
        return BASE_STORAGE_BYTES + getTotalStorageCapacity();
    }

    /**
     * 获取剩余可用存储空间（字节）
     */
    public long getRemainingStorageBytes() {
        return getTotalStorageBytes() - getUsedStorageBytes();
    }

    // ==================== 模式切换 ====================

    public Mode getMode() {
        return mode;
    }

    public void setMode(Mode mode) {
        if (this.mode == mode) {
            return;
        }
        this.mode = mode;
        threadManager.cleanup(List.of());
        updateJobs();
        setChanged();
        // 主动向客户端同步，使模式切换按钮和 GUI 状态立即更新
        syncToClient();
    }

    /**
     * 向客户端推送方块实体更新（模式等需要客户端同步的状态）。
     */
    public void syncToClient() {
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
        }
    }

    // ==================== 访问器 ====================

    public SimpleEnergyStorage getEnergy() {
        return energy;
    }

    public ItemStackHandler getInventory() {
        return inventory;
    }

    public List<StoredData> getStoredData() {
        return storedData;
    }

    public ThreadManager getThreadManager() {
        return threadManager;
    }

    /** 是否处于工作状态（读取 blockstate，客户端/服务器均可用） */
    public boolean isWorking() {
        return getBlockState().getValue(DataTerminalBlock.WORKING);
    }

    // ==================== 菜单提供 ====================

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.createmassenergy.data_terminal");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new DataTerminalMenu(containerId, inventory, this);
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
        return Set.of(UpgradeType.THREAD, UpgradeType.STORAGE);
    }

    public UpgradeSlotHandler getUpgradeSlots() {
        return upgradeSlots;
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
        event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, ModBlockEntities.DATA_TERMINAL.get(),
            (be, direction) -> be.getEnergy());
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ModBlockEntities.DATA_TERMINAL.get(),
            (be, direction) -> be.getInventory());
    }

    // ==================== 数据保存 ====================

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("Inventory", inventory.serializeNBT(registries));
        tag.put("UpgradeSlots", upgradeSlots.serializeNBT(registries));
        tag.putInt("Energy", energy.getEnergyStored());
        tag.putString("Mode", mode.name());
        // 存储数据
        ListTag list = new ListTag();
        for (StoredData data : storedData) {
            CompoundTag entry = new CompoundTag();
            entry.put("Item", data.item.saveOptional(registries));
            entry.putLong("Count", data.count);
            list.add(entry);
        }
        tag.put("StoredData", list);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        inventory.deserializeNBT(registries, tag.getCompound("Inventory"));
        upgradeSlots.deserializeNBT(registries, tag.getCompound("UpgradeSlots"));
        energy.setEnergy(tag.getInt("Energy"));
        if (tag.contains("Mode")) {
            try {
                mode = Mode.valueOf(tag.getString("Mode"));
            } catch (IllegalArgumentException ignored) {
                mode = Mode.DECOMPOSE;
            }
        }
        storedData.clear();
        ListTag list = tag.getList("StoredData", 10);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entry = list.getCompound(i);
            ItemStack item = ItemStack.parseOptional(registries, entry.getCompound("Item"));
            long count = entry.getLong("Count");
            if (!item.isEmpty() && count > 0) {
                storedData.add(new StoredData(item, count));
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
