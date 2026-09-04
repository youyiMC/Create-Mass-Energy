package com.youyimc.createmassenergy.block.entity;

import java.util.List;
import java.util.Set;

import com.youyimc.createmassenergy.ModBlockEntities;
import com.youyimc.createmassenergy.block.AnnihilationFurnaceBlock;
import com.youyimc.createmassenergy.energy.SimpleEnergyStorage;
import com.youyimc.createmassenergy.menu.AnnihilationFurnaceMenu;
import com.youyimc.createmassenergy.thread.ThreadManager;
import com.youyimc.createmassenergy.upgrade.UpgradeContainer;
import com.youyimc.createmassenergy.upgrade.UpgradeSlotHandler;
import com.youyimc.createmassenergy.upgrade.UpgradeType;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
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
 * 物品湮灭炉方块实体
 * <p>
 * 处理逻辑：从输入槽取物品 → 按质能方程 E = q·t² 计算能量（每物品 400 FE）→ 实际产出 ×60% = 240 FE/物品
 * 线程：默认同种物品 1 线程，可升级多线程拆分。
 */
public class AnnihilationFurnaceBlockEntity extends BlockEntity implements UpgradeContainer, MenuProvider {

    /** 输入槽数量 */
    public static final int INPUT_SLOTS = 18;
    /** 能量缓冲容量 (FE) */
    public static final int ENERGY_CAPACITY = 100_000;
    /** 每 tick 最大能量输出 */
    public static final int MAX_OUTPUT = 4_000;
    /** 基础处理速度：20 物品/秒/线程 */
    public static final double BASE_SPEED_PER_TICK = 20.0 / 20.0;
    /** 质能方程常数 t */
    public static final int ENERGY_CONSTANT = 20;
    /** 能量损耗比例：实际产出 = 理论 × (1 - 损耗) */
    public static final double LOSS_RATIO = 0.4;

    /** 输入物品栏 */
    private final ItemStackHandler inputInventory = new ItemStackHandler(INPUT_SLOTS) {
        @Override
        protected void onContentsChanged(int slot) {
            super.onContentsChanged(slot);
            setChanged();
            updateJobs();
        }
    };

    /** 升级插槽 */
    private final UpgradeSlotHandler upgradeSlots = new UpgradeSlotHandler(
        Set.of(UpgradeType.THREAD), // 湮灭炉只能装线程升级
        () -> {
            setChanged();
            updateThreads();
            checkUpgradeAchievements();
        });

    /** 能量缓冲（只输出） */
    private final SimpleEnergyStorage energy = new SimpleEnergyStorage(ENERGY_CAPACITY, 0, MAX_OUTPUT) {
        @Override
        protected void onEnergyChanged() {
            setChanged();
        }
    };

    /** 线程管理器 */
    private final ThreadManager threadManager = new ThreadManager((item, count) -> processItem(item, count));

    private int cooldown = 0;

    public AnnihilationFurnaceBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ANNIHILATION_FURNACE.get(), pos, state);
    }

    // ==================== 服务器 tick ====================
    public static void serverTick(Level level, BlockPos pos, BlockState state, AnnihilationFurnaceBlockEntity be) {
        be.tick();
    }

    private void tick() {
        if (level == null || level.isClientSide) {
            return;
        }

        // 尝试从输入槽补充作业
        updateJobs();
        // 推进处理：每个游戏 tick（20/s）执行，使销毁达到设计基线 20 物品/秒/线程
        threadManager.tick();

        // 能量输出与工作状态刷新：每 0.5 秒节流一次，减少不必要的 setChanged/发包开销
        cooldown--;
        if (cooldown <= 0) {
            cooldown = 10; // 每 0.5 秒
            // 向相邻方块输出能量
            pushEnergy();

            // 更新工作状态（渲染）
            boolean working = !threadManager.isEmpty();
            if (working != getBlockState().getValue(AnnihilationFurnaceBlock.WORKING)) {
                level.setBlock(getBlockPos(), getBlockState().setValue(AnnihilationFurnaceBlock.WORKING, working), 3);
            }
        }
    }

    /**
     * 从输入槽为每种物品建立作业
     */
    private void updateJobs() {
        boolean hasItem = false;
        for (int i = 0; i < inputInventory.getSlots(); i++) {
            ItemStack stack = inputInventory.getStackInSlot(i);
            if (!stack.isEmpty()) {
                hasItem = true;
                threadManager.ensureJobFor(stack);
            }
        }
        if (!hasItem) {
            threadManager.cleanup(List.of());
        }
    }

    /**
     * 处理指定物品（由线程管理器调用），销毁物品并产出能量
     */
    private void processItem(ItemStack item, int count) {
        // 从输入槽中扣除对应物品
        int remaining = count;
        for (int i = 0; i < inputInventory.getSlots() && remaining > 0; i++) {
            ItemStack stack = inputInventory.getStackInSlot(i);
            if (!stack.isEmpty() && ItemStack.isSameItemSameComponents(stack, item)) {
                int toRemove = Math.min(remaining, stack.getCount());
                stack.shrink(toRemove);
                remaining -= toRemove;
                if (stack.isEmpty()) {
                    inputInventory.setStackInSlot(i, ItemStack.EMPTY);
                } else {
                    inputInventory.setStackInSlot(i, stack);
                }
            }
        }
        if (remaining > 0) {
            // 库存不足，作业失效
            return;
        }

        // 质能方程：E = q · t²，每物品 400 FE；实际产出 × (1 - 损耗率)
        long theoretical = (long) count * ENERGY_CONSTANT * ENERGY_CONSTANT;
        int actual = (int) (theoretical * (1.0 - LOSS_RATIO));
        energy.addEnergy(actual);
        setChanged();
    }

    /**
     * 更新线程数（升级模块变化时调用）
     */
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

    /**
     * 向相邻方块输出能量
     */
    private void pushEnergy() {
        if (energy.getEnergyStored() <= 0 || level == null) {
            return;
        }
        for (Direction direction : Direction.values()) {
            BlockPos neighbor = getBlockPos().relative(direction);
            var cap = level.getCapability(Capabilities.EnergyStorage.BLOCK, neighbor, direction.getOpposite());
            if (cap != null && cap.canReceive()) {
                int transferred = cap.receiveEnergy(energy.extractEnergy(energy.getEnergyStored(), true), false);
                if (transferred > 0) {
                    energy.consumeEnergy(transferred);
                }
            }
        }
    }

    // ==================== 能量与物品访问 ====================

    public SimpleEnergyStorage getEnergy() {
        return energy;
    }

    public ItemStackHandler getInputInventory() {
        return inputInventory;
    }

    public ThreadManager getThreadManager() {
        return threadManager;
    }

    // ==================== 菜单提供 ====================

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.createmassenergy.annihilation_furnace");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new AnnihilationFurnaceMenu(containerId, inventory, this);
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

    public UpgradeSlotHandler getUpgradeSlots() {
        return upgradeSlots;
    }

    // ==================== 能力注册 ====================

    /**
     * 注册能力（能量输出 + 物品输入）
     */
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, ModBlockEntities.ANNIHILATION_FURNACE.get(),
            (be, direction) -> be.getEnergy());
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ModBlockEntities.ANNIHILATION_FURNACE.get(),
            (be, direction) -> be.getInputInventory());
    }

    // ==================== 数据保存 ====================

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("InputInventory", inputInventory.serializeNBT(registries));
        tag.put("UpgradeSlots", upgradeSlots.serializeNBT(registries));
        tag.putInt("Energy", energy.getEnergyStored());
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        inputInventory.deserializeNBT(registries, tag.getCompound("InputInventory"));
        upgradeSlots.deserializeNBT(registries, tag.getCompound("UpgradeSlots"));
        energy.setEnergy(tag.getInt("Energy"));
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
