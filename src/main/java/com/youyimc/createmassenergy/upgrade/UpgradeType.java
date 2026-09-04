package com.youyimc.createmassenergy.upgrade;

import java.util.Locale;

/**
 * 升级模块类型
 */
public enum UpgradeType {
    /** 线程升级（多核心处理器） */
    THREAD,
    /** 存储升级（软盘/硬盘） */
    STORAGE;

    public String getSerializedName() {
        return name().toLowerCase(Locale.ROOT);
    }
}
