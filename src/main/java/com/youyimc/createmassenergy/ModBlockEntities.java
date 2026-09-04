package com.youyimc.createmassenergy;

import com.youyimc.createmassenergy.block.entity.AnnihilationFurnaceBlockEntity;
import com.youyimc.createmassenergy.block.entity.DataTerminalBlockEntity;
import com.youyimc.createmassenergy.block.entity.RadioTelegraphBlockEntity;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * 方块实体类型注册
 */
public final class ModBlockEntities {
    private ModBlockEntities() {}

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
        DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, CreateMassenergy.MODID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<AnnihilationFurnaceBlockEntity>> ANNIHILATION_FURNACE =
        BLOCK_ENTITIES.register("annihilation_furnace",
            () -> BlockEntityType.Builder.of(AnnihilationFurnaceBlockEntity::new, ModBlocks.ANNIHILATION_FURNACE.get())
                .build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<DataTerminalBlockEntity>> DATA_TERMINAL =
        BLOCK_ENTITIES.register("data_terminal",
            () -> BlockEntityType.Builder.of(DataTerminalBlockEntity::new, ModBlocks.DATA_TERMINAL.get())
                .build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<RadioTelegraphBlockEntity>> RADIO_TELEGRAPH =
        BLOCK_ENTITIES.register("radio_telegraph",
            () -> BlockEntityType.Builder.of(RadioTelegraphBlockEntity::new, ModBlocks.RADIO_TELEGRAPH.get())
                .build(null));
}
