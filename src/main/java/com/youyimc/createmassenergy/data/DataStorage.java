package com.youyimc.createmassenergy.data;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;

/**
 * 物品数据化存储的核心数据模型。
 * <p>
 * 存储规则：
 * <ul>
 *   <li>最多堆叠64个的物品（木板、石头、小麦等）：每物品 16KB</li>
 *   <li>最多堆叠16个的物品（雪球、末影珍珠等）：每物品 32KB</li>
 *   <li>不可堆叠的物品（剑、斧等）：每物品 256KB</li>
 * </ul>
 * <p>
 * 文档规定："任何最多被堆叠为64的物品……每一个这样的物品占用16KB；任何最多被堆叠为16的物品……每一个这样的物品占用32KB；任何无法被堆叠的物品……每一个占用256KB。"
 * 按文档"堆叠为64"与"堆叠为16"的表述，这里按 maxStackSize 归入最接近的一档：stackSize >= 16 视为"可堆叠16"档（32KB），
 * stackSize >= 2 视为"可堆叠64"档（16KB），stackSize == 1 视为不可堆叠（256KB）。
 * 但为了贴合文档语义（最多堆叠64的物品占16KB、最多堆叠16的物品占32KB），
 * 我们以 maxStackSize == 1 → 256KB；maxStackSize <= 16 → 32KB；其余 → 16KB。
 */
public final class DataStorage {
    private DataStorage() {}

    /** 堆叠64物品：16KB/个 */
    public static final long BYTES_PER_STACK64 = 16L * 1024L;
    /** 堆叠16物品：32KB/个 */
    public static final long BYTES_PER_STACK16 = 32L * 1024L;
    /** 不可堆叠物品：256KB/个 */
    public static final long BYTES_PER_UNSTACKABLE = 256L * 1024L;
    /** 每一条 NBT/组件标签：4KB */
    public static final long BYTES_PER_NBT_TAG = 4L * 1024L;

    /**
     * 计算单个物品占用的存储字节数
     */
    public static long bytesPerItem(Item item) {
        int maxStack = item.getDefaultMaxStackSize();
        if (maxStack <= 1) {
            return BYTES_PER_UNSTACKABLE;
        } else if (maxStack <= 16) {
            return BYTES_PER_STACK16;
        } else {
            return BYTES_PER_STACK64;
        }
    }

    /**
     * 计算单个物品占用的存储字节数
     */
    public static long bytesPerItem(ItemStack stack) {
        return bytesPerItem(stack.getItem());
    }

    /**
     * 计算单个物品带有的“自定义 NBT/组件标签”数量（每条 4KB）。
     * <p>
     * 使用 {@link ItemStack#getComponentsPatch()} 统计与物品默认定义不同的组件
     * （附魔、自定义名、损坏值、纸包裹内容等真正需要区分/保存的数据），
     * 天然排除了物品固有属性（如最大堆叠、材质等不会出现在 patch 中）。
     */
    public static int nbtTagCount(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return 0;
        }
        try {
            return stack.getComponentsPatch().size();
        } catch (Throwable t) {
            return 0;
        }
    }

    /**
     * 计算单个物品的 NBT 组件占用字节（每条 4KB）
     */
    public static long nbtBytesPerItem(ItemStack stack) {
        return (long) nbtTagCount(stack) * BYTES_PER_NBT_TAG;
    }

    /**
     * 计算 N 个物品占用的总存储字节数（含 NBT 组件费用）
     */
    public static long bytesFor(ItemStack stack, long count) {
        if (stack == null || stack.isEmpty() || count <= 0) {
            return 0;
        }
        long base = bytesPerItem(stack.getItem()) * count;
        long nbt = nbtBytesPerItem(stack) * count;
        return base + nbt;
    }

    /**
     * 判断物品是否可以被数据化（所有物品都可以）
     */
    public static boolean isDataizable(ItemStack stack) {
        return !stack.isEmpty();
    }

    /**
     * 将容量字节数格式化为可读字符串（KB/MB/GB/TB）
     */
    public static String formatBytes(long bytes) {
        if (bytes >= 1024L * 1024L * 1024L * 1024L) {
            return String.format("%.2f TB", bytes / (1024.0 * 1024 * 1024 * 1024));
        } else if (bytes >= 1024L * 1024L * 1024L) {
            return String.format("%.2f GB", bytes / (1024.0 * 1024 * 1024));
        } else if (bytes >= 1024L * 1024L) {
            return String.format("%.2f MB", bytes / (1024.0 * 1024));
        } else if (bytes >= 1024L) {
            return String.format("%.2f KB", bytes / 1024.0);
        } else {
            return bytes + " B";
        }
    }

    /**
     * 根据 ItemStack 生成稳定唯一的存储键
     */
    public static String keyOf(ItemStack stack) {
        ResourceLocation key = BuiltInRegistries.ITEM.getKey(stack.getItem());
        // 带 NBT 的物品区分存储键（不同 NBT 的物品视为不同数据）
        return key.toString() + "|" + stack.getComponentsPatch().hashCode();
    }
}
