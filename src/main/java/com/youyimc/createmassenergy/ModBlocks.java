package com.youyimc.createmassenergy;

import com.youyimc.createmassenergy.block.AnnihilationFurnaceBlock;
import com.youyimc.createmassenergy.block.DataTerminalBlock;
import com.youyimc.createmassenergy.block.RadioTelegraphBlock;

import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * 模组方块注册
 */
public final class ModBlocks {
    private ModBlocks() {}

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(CreateMassenergy.MODID);

    /** 物品湮灭炉 */
    public static final DeferredBlock<AnnihilationFurnaceBlock> ANNIHILATION_FURNACE = BLOCKS.register("annihilation_furnace",
        () -> new AnnihilationFurnaceBlock(BlockBehaviour.Properties.of()
            .strength(3.0f, 8.0f)
            .requiresCorrectToolForDrops()));

    /** 数据化终端 */
    public static final DeferredBlock<DataTerminalBlock> DATA_TERMINAL = BLOCKS.register("data_terminal",
        () -> new DataTerminalBlock(BlockBehaviour.Properties.of()
            .strength(3.0f, 8.0f)
            .requiresCorrectToolForDrops()));

    /** 收发报机 */
    public static final DeferredBlock<RadioTelegraphBlock> RADIO_TELEGRAPH = BLOCKS.register("radio_telegraph",
        () -> new RadioTelegraphBlock(BlockBehaviour.Properties.of()
            .strength(3.0f, 8.0f)
            .requiresCorrectToolForDrops()));
}
