package com.youyimc.createmassenergy;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * 模组配置
 */
public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    /** 质能方程常数 t */
    public static final ModConfigSpec.IntValue ENERGY_CONSTANT = BUILDER
        .comment("质能方程常数 t (E = q·t²)")
        .defineInRange("energyConstant", 20, 1, 1000);

    /** 湮灭炉能量损耗比例 */
    public static final ModConfigSpec.DoubleValue ANNIHILATION_LOSS = BUILDER
        .comment("物品湮灭炉能量损耗比例 (实际产出 = 理论 × (1 - 损耗))")
        .defineInRange("annihilationLoss", 0.4, 0.0, 1.0);

    /** 数据化终端分解损耗比例 */
    public static final ModConfigSpec.DoubleValue DECOMPOSE_LOSS = BUILDER
        .comment("数据化分解能量损耗比例")
        .defineInRange("decomposeLoss", 0.5, 0.0, 1.0);

    /** 湮灭炉处理速度（物品/秒/线程） */
    public static final ModConfigSpec.IntValue ANNIHILATION_SPEED = BUILDER
        .comment("物品湮灭炉处理速度（物品/秒/线程）")
        .defineInRange("annihilationSpeed", 20, 1, 1000);

    /** 数据化终端处理速度（物品/秒/线程） */
    public static final ModConfigSpec.IntValue TERMINAL_SPEED = BUILDER
        .comment("数据化终端处理速度（物品/秒/线程）")
        .defineInRange("terminalSpeed", 5, 1, 1000);

    static final ModConfigSpec SPEC = BUILDER.build();
}
