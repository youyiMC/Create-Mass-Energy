package com.youyimc.createmassenergy.upgrade;

/**
 * 升级模块定义
 * 每种升级模块有：类型、等级、数值（线程数或存储容量）
 */
public record UpgradeDefinition(UpgradeType type, String level, long value, String descriptionKey) {

    // ==================== 线程升级（多核心处理器） ====================
    public static final UpgradeDefinition DUAL_CORE = new UpgradeDefinition(UpgradeType.THREAD, "dual", 2,
        "upgrade.createmassenergy.dual_core");
    public static final UpgradeDefinition QUAD_CORE = new UpgradeDefinition(UpgradeType.THREAD, "quad", 4,
        "upgrade.createmassenergy.quad_core");
    public static final UpgradeDefinition OCTO_CORE = new UpgradeDefinition(UpgradeType.THREAD, "octo", 8,
        "upgrade.createmassenergy.octo_core");
    public static final UpgradeDefinition HEXADECIMAL_CORE = new UpgradeDefinition(UpgradeType.THREAD, "hexadecimal", 16,
        "upgrade.createmassenergy.hexadecimal_core");
    public static final UpgradeDefinition DOTRIDECIMAL_CORE = new UpgradeDefinition(UpgradeType.THREAD, "dotridecimal", 32,
        "upgrade.createmassenergy.dotridecimal_core");

    // ==================== 存储升级（软盘/硬盘） ====================
    /** 1.2MB 软盘 */
    public static final UpgradeDefinition FLOPPY_DISK_525 = new UpgradeDefinition(UpgradeType.STORAGE, "1.2mb", 1_200_000L,
        "upgrade.createmassenergy.floppy_disk_525");
    /** 1.44MB 软盘 */
    public static final UpgradeDefinition FLOPPY_DISK_35 = new UpgradeDefinition(UpgradeType.STORAGE, "1.44mb", 1_440_000L,
        "upgrade.createmassenergy.floppy_disk_35");
    /** 16GB 硬盘 */
    public static final UpgradeDefinition HARD_DISK_16GB = new UpgradeDefinition(UpgradeType.STORAGE, "16gb", 16_000_000_000L,
        "upgrade.createmassenergy.hard_disk_16gb");
    /** 64GB 硬盘 */
    public static final UpgradeDefinition HARD_DISK_64GB = new UpgradeDefinition(UpgradeType.STORAGE, "64gb", 64_000_000_000L,
        "upgrade.createmassenergy.hard_disk_64gb");
    /** 128GB 硬盘 */
    public static final UpgradeDefinition HARD_DISK_128GB = new UpgradeDefinition(UpgradeType.STORAGE, "128gb", 128_000_000_000L,
        "upgrade.createmassenergy.hard_disk_128gb");
    /** 512GB 硬盘 */
    public static final UpgradeDefinition HARD_DISK_512GB = new UpgradeDefinition(UpgradeType.STORAGE, "512gb", 512_000_000_000L,
        "upgrade.createmassenergy.hard_disk_512gb");
}
