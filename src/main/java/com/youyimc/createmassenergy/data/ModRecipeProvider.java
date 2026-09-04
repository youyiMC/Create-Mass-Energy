package com.youyimc.createmassenergy.data;

import java.util.concurrent.CompletableFuture;

import com.youyimc.createmassenergy.ModItems;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Blocks;

/**
 * 合成配方生成器
 * <p>
 * 所有配方按文档规定，使用 Create 物品与原版物品混合。
 * Create 物品通过注册名引用（如 create:andesite_casing）。
 */
public class ModRecipeProvider extends RecipeProvider {

    public ModRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    /** 通过注册名获取 Create 物品 */
    private static Item createItem(String path) {
        return BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("create", path));
    }

    private static Ingredient createIngredient(String path) {
        return Ingredient.of(createItem(path));
    }
    @Override
    protected void buildRecipes(RecipeOutput output) {
        // ==================== 物品湮灭炉 ====================
        // 安山机壳 | 铜锭（原版） | 安山机壳
        // 铜锭（原版） | 高炉（原版） | 铜锭（原版）
        // 安山机壳 | 铜锭（原版） | 安山机壳
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.ANNIHILATION_FURNACE_ITEM.get())
            .pattern("ACA")
            .pattern("CBC")
            .pattern("ACA")
            .define('A', createIngredient("andesite_casing"))
            .define('C', Items.COPPER_INGOT)
            .define('B', Blocks.BLAST_FURNACE)
            .unlockedBy("has_blast_furnace", has(Blocks.BLAST_FURNACE))
            .save(output);

        // ==================== 数据化终端 ====================
        // 安山机壳 | 无线红石信号终端 | 安山机壳
        // 安山机壳 | 物品湮灭炉 | 安山机壳
        // 安山机壳 | 安山机壳 | 安山机壳
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.DATA_TERMINAL_ITEM.get())
            .pattern("ARA")
            .pattern("AFA")
            .pattern("AAA")
            .define('A', createIngredient("andesite_casing"))
            .define('R', createIngredient("redstone_link"))
            .define('F', ModItems.ANNIHILATION_FURNACE_ITEM.get())
            .unlockedBy("has_annihilation_furnace", has(ModItems.ANNIHILATION_FURNACE_ITEM.get()))
            .save(output);

        // ==================== 收发报机 ====================
        // 铜机壳 | 避雷针（原版） | 铜机壳
        // 铜机壳 | 指南针（原版） | 铜机壳
        // 铜机壳 | 铜机壳 | 铜机壳
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.RADIO_TELEGRAPH_ITEM.get())
            .pattern("CRC")
            .pattern("CSC")
            .pattern("CCC")
            .define('C', createIngredient("copper_casing"))
            .define('R', Items.LIGHTNING_ROD)
            .define('S', Items.COMPASS)
            .unlockedBy("has_compass", has(Items.COMPASS))
            .save(output);

        // ==================== 双核处理器 ====================
        // 铜锭（原版） | 时钟（原版） | 铜锭（原版）
        // 金锭（原版） | 脉冲计时器 | 金锭（原版）
        // 铜锭（原版） | 铜锭（原版） | 铜锭（原版）
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.DUAL_CORE.get())
            .pattern("CKC")
            .pattern("GTG")
            .pattern("CCC")
            .define('C', Items.COPPER_INGOT)
            .define('K', Items.CLOCK)
            .define('G', Items.GOLD_INGOT)
            .define('T', createIngredient("pulse_timer"))
            .unlockedBy("has_pulse_timer", has(createItem("pulse_timer")))
            .save(output);

        // ==================== 4核处理器（无序） ====================
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.QUAD_CORE.get())
            .requires(ModItems.DUAL_CORE.get(), 2)
            .unlockedBy("has_dual_core", has(ModItems.DUAL_CORE.get()))
            .save(output);

        // ==================== 8核处理器（无序） ====================
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.OCTO_CORE.get())
            .requires(ModItems.QUAD_CORE.get(), 2)
            .unlockedBy("has_quad_core", has(ModItems.QUAD_CORE.get()))
            .save(output);

        // ==================== 16核处理器（无序） ====================
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.HEXADECIMAL_CORE.get())
            .requires(ModItems.OCTO_CORE.get(), 2)
            .unlockedBy("has_octo_core", has(ModItems.OCTO_CORE.get()))
            .save(output);

        // ==================== 32核处理器（无序） ====================
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.DOTRIDECIMAL_CORE.get())
            .requires(ModItems.HEXADECIMAL_CORE.get(), 2)
            .unlockedBy("has_hexadecimal_core", has(ModItems.HEXADECIMAL_CORE.get()))
            .save(output);

        // ==================== 5.25英寸软盘（1.2MB） ====================
        // 纸板 | 红石粉 | 纸板
        // 纸板 | 转盘 | 纸板
        // 纸板 | 纸板 | 纸板
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.FLOPPY_DISK_525.get())
            .pattern("PRP")
            .pattern("PTP")
            .pattern("PPP")
            .define('P', createIngredient("cardboard"))
            .define('R', Items.REDSTONE)
            .define('T', createIngredient("turntable"))
            .unlockedBy("has_cardboard", has(createItem("cardboard")))
            .save(output);

        // ==================== 3.5英寸软盘（1.44MB） ====================
        // 纸板 | 红石粉*2 | 纸板
        // 纸板 | 转盘*2 | 纸板
        // 纸板 | 纸板 | 纸板
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.FLOPPY_DISK_35.get())
            .pattern("PRP")
            .pattern("PTP")
            .pattern("PPP")
            .define('P', createIngredient("cardboard"))
            .define('R', Ingredient.of(Items.REDSTONE, Items.REDSTONE)) // 2个红石粉
            .define('T', Ingredient.of(createItem("turntable"), createItem("turntable"))) // 2个转盘
            .unlockedBy("has_floppy_disk_525", has(ModItems.FLOPPY_DISK_525.get()))
            .save(output);

        // ==================== 16GB硬盘 ====================
        // 铁板 | 转换锁存器 | 铁板
        // 铁板 | 转盘 | 铁板
        // 铁板 | 铁板 | 铁板
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.HARD_DISK_16GB.get())
            .pattern("ILI")
            .pattern("ITI")
            .pattern("III")
            .define('I', createIngredient("iron_sheet"))
            .define('L', createIngredient("powered_latch"))
            .define('T', createIngredient("turntable"))
            .unlockedBy("has_iron_sheet", has(createItem("iron_sheet")))
            .save(output);

        // ==================== 硬盘升级（无序） ====================
        // 16GB + 转盘*3 + 红石粉*3 → 64GB
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.HARD_DISK_64GB.get())
            .requires(ModItems.HARD_DISK_16GB.get())
            .requires(createItem("turntable"), 3)
            .requires(Items.REDSTONE, 3)
            .unlockedBy("has_hard_disk_16gb", has(ModItems.HARD_DISK_16GB.get()))
            .save(output);

        // 64GB + 转盘*4 + 红石粉*4 → 128GB
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.HARD_DISK_128GB.get())
            .requires(ModItems.HARD_DISK_64GB.get())
            .requires(createItem("turntable"), 4)
            .requires(Items.REDSTONE, 4)
            .unlockedBy("has_hard_disk_64gb", has(ModItems.HARD_DISK_64GB.get()))
            .save(output);

        // 128GB + 转盘*24 + 红石粉*24 → 512GB
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.HARD_DISK_512GB.get())
            .requires(ModItems.HARD_DISK_128GB.get())
            .requires(createItem("turntable"), 24)
            .requires(Items.REDSTONE, 24)
            .unlockedBy("has_hard_disk_128gb", has(ModItems.HARD_DISK_128GB.get()))
            .save(output);
    }
}
