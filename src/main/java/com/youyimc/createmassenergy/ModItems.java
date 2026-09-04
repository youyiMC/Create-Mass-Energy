package com.youyimc.createmassenergy;

import com.youyimc.createmassenergy.item.UpgradeItem;
import com.youyimc.createmassenergy.upgrade.UpgradeDefinition;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * 模组物品注册
 */
public final class ModItems {
    private ModItems() {}

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(CreateMassenergy.MODID);

    // ==================== 方块物品 ====================
    public static final DeferredItem<BlockItem> ANNIHILATION_FURNACE_ITEM = ITEMS.registerSimpleBlockItem(
        "annihilation_furnace", ModBlocks.ANNIHILATION_FURNACE);
    public static final DeferredItem<BlockItem> DATA_TERMINAL_ITEM = ITEMS.registerSimpleBlockItem(
        "data_terminal", ModBlocks.DATA_TERMINAL);
    public static final DeferredItem<BlockItem> RADIO_TELEGRAPH_ITEM = ITEMS.registerSimpleBlockItem(
        "radio_telegraph", ModBlocks.RADIO_TELEGRAPH);

    // ==================== 线程升级（多核心处理器） ====================
    /** 双核处理器（2线程） */
    public static final DeferredItem<UpgradeItem> DUAL_CORE = ITEMS.register("dual_core_processor",
        () -> new UpgradeItem(new Item.Properties(), UpgradeDefinition.DUAL_CORE));
    /** 4核处理器（4线程） */
    public static final DeferredItem<UpgradeItem> QUAD_CORE = ITEMS.register("quad_core_processor",
        () -> new UpgradeItem(new Item.Properties(), UpgradeDefinition.QUAD_CORE));
    /** 8核处理器（8线程） */
    public static final DeferredItem<UpgradeItem> OCTO_CORE = ITEMS.register("octo_core_processor",
        () -> new UpgradeItem(new Item.Properties(), UpgradeDefinition.OCTO_CORE));
    /** 16核处理器（16线程） */
    public static final DeferredItem<UpgradeItem> HEXADECIMAL_CORE = ITEMS.register("hexadecimal_core_processor",
        () -> new UpgradeItem(new Item.Properties(), UpgradeDefinition.HEXADECIMAL_CORE));
    /** 32核处理器（32线程） */
    public static final DeferredItem<UpgradeItem> DOTRIDECIMAL_CORE = ITEMS.register("dotridecimal_core_processor",
        () -> new UpgradeItem(new Item.Properties(), UpgradeDefinition.DOTRIDECIMAL_CORE));

    // ==================== 存储升级（软盘） ====================
    /** 5.25英寸软盘（1.2MB） */
    public static final DeferredItem<UpgradeItem> FLOPPY_DISK_525 = ITEMS.register("floppy_disk_525",
        () -> new UpgradeItem(new Item.Properties().stacksTo(1), UpgradeDefinition.FLOPPY_DISK_525));
    /** 3.5英寸软盘（1.44MB） */
    public static final DeferredItem<UpgradeItem> FLOPPY_DISK_35 = ITEMS.register("floppy_disk_35",
        () -> new UpgradeItem(new Item.Properties().stacksTo(1), UpgradeDefinition.FLOPPY_DISK_35));

    // ==================== 存储升级（硬盘） ====================
    /** 16GB 硬盘 */
    public static final DeferredItem<UpgradeItem> HARD_DISK_16GB = ITEMS.register("hard_disk_16gb",
        () -> new UpgradeItem(new Item.Properties().stacksTo(1), UpgradeDefinition.HARD_DISK_16GB));
    /** 64GB 硬盘 */
    public static final DeferredItem<UpgradeItem> HARD_DISK_64GB = ITEMS.register("hard_disk_64gb",
        () -> new UpgradeItem(new Item.Properties().stacksTo(1), UpgradeDefinition.HARD_DISK_64GB));
    /** 128GB 硬盘 */
    public static final DeferredItem<UpgradeItem> HARD_DISK_128GB = ITEMS.register("hard_disk_128gb",
        () -> new UpgradeItem(new Item.Properties().stacksTo(1), UpgradeDefinition.HARD_DISK_128GB));
    /** 512GB 硬盘 */
    public static final DeferredItem<UpgradeItem> HARD_DISK_512GB = ITEMS.register("hard_disk_512gb",
        () -> new UpgradeItem(new Item.Properties().stacksTo(1), UpgradeDefinition.HARD_DISK_512GB));

    // ==================== 节日彩蛋成就图标物品（隐藏，不进创造栏） ====================
    /** 注册一个仅作为成就图标的隐藏物品 */
    private static DeferredItem<Item> registerFestivalIcon(String name) {
        return ITEMS.register(name, () -> new Item(new Item.Properties().stacksTo(1)));
    }

    public static final DeferredItem<Item> FEST_ICON_1_7 = registerFestivalIcon("fest_icon_1_7");
    public static final DeferredItem<Item> FEST_ICON_2_24 = registerFestivalIcon("fest_icon_2_24");
    public static final DeferredItem<Item> FEST_ICON_3_28 = registerFestivalIcon("fest_icon_3_28");
    public static final DeferredItem<Item> FEST_ICON_7_7 = registerFestivalIcon("fest_icon_7_7");
    public static final DeferredItem<Item> FEST_ICON_7_28 = registerFestivalIcon("fest_icon_7_28");
    public static final DeferredItem<Item> FEST_ICON_9_10 = registerFestivalIcon("fest_icon_9_10");
    public static final DeferredItem<Item> FEST_ICON_9_11 = registerFestivalIcon("fest_icon_9_11");
    public static final DeferredItem<Item> FEST_ICON_12_25 = registerFestivalIcon("fest_icon_12_25");
    public static final DeferredItem<Item> FEST_ICON_12_26 = registerFestivalIcon("fest_icon_12_26");

    /** 永不消逝的电波（集齐所有彩蛋音效）成就图标 */
    public static final DeferredItem<Item> FEST_ICON_ETERNAL_WAVE = registerFestivalIcon("fest_icon_eternal_wave");
}
