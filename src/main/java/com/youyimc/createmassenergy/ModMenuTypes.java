package com.youyimc.createmassenergy;

import com.youyimc.createmassenergy.menu.AnnihilationFurnaceMenu;
import com.youyimc.createmassenergy.menu.DataTerminalMenu;
import com.youyimc.createmassenergy.menu.RadioTelegraphMenu;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.network.IContainerFactory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * 菜单类型注册
 */
public final class ModMenuTypes {
    private ModMenuTypes() {}

    public static final DeferredRegister<MenuType<?>> MENUS =
        DeferredRegister.create(Registries.MENU, CreateMassenergy.MODID);

    public static final DeferredHolder<MenuType<?>, MenuType<AnnihilationFurnaceMenu>> ANNIHILATION_FURNACE =
        MENUS.register("annihilation_furnace", () -> IMenuTypeExtension.create(AnnihilationFurnaceMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<DataTerminalMenu>> DATA_TERMINAL =
        MENUS.register("data_terminal", () -> IMenuTypeExtension.create(DataTerminalMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<RadioTelegraphMenu>> RADIO_TELEGRAPH =
        MENUS.register("radio_telegraph", () -> IMenuTypeExtension.create(RadioTelegraphMenu::new));
}
