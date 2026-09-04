package com.youyimc.createmassenergy;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * 创造模式标签页注册
 */
public final class ModCreativeTabs {
    private ModCreativeTabs() {}

    public static final DeferredRegister<CreativeModeTab> TABS =
        DeferredRegister.create(Registries.CREATIVE_MODE_TAB, CreateMassenergy.MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MAIN_TAB = TABS.register("main", () ->
        CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.createmassenergy"))
            .icon(() -> new ItemStack(ModBlocks.ANNIHILATION_FURNACE.get()))
            .displayItems((params, output) -> {
                // 方块
                output.accept(ModBlocks.ANNIHILATION_FURNACE.get());
                output.accept(ModBlocks.DATA_TERMINAL.get());
                output.accept(ModBlocks.RADIO_TELEGRAPH.get());
                // 升级模块
                output.accept(ModItems.DUAL_CORE.get());
                output.accept(ModItems.QUAD_CORE.get());
                output.accept(ModItems.OCTO_CORE.get());
                output.accept(ModItems.HEXADECIMAL_CORE.get());
                output.accept(ModItems.DOTRIDECIMAL_CORE.get());
                output.accept(ModItems.FLOPPY_DISK_525.get());
                output.accept(ModItems.FLOPPY_DISK_35.get());
                output.accept(ModItems.HARD_DISK_16GB.get());
                output.accept(ModItems.HARD_DISK_64GB.get());
                output.accept(ModItems.HARD_DISK_128GB.get());
                output.accept(ModItems.HARD_DISK_512GB.get());
            })
            .build());
}
