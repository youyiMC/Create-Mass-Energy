package com.youyimc.createmassenergy.advancement;

import java.util.HashSet;
import java.util.Set;

import com.youyimc.createmassenergy.item.UpgradeItem;
import com.youyimc.createmassenergy.upgrade.UpgradeContainer;
import com.youyimc.createmassenergy.upgrade.UpgradeType;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * 成就触发辅助工具
 * <p>
 * 在升级插槽变更时调用，检查并触发对应成就。
 */
public final class AdvancementHelper {
    private AdvancementHelper() {}

    /**
     * 在升级插槽内容变更后调用，检查成就
     *
     * @param container 升级容器
     * @param player 附近玩家（或 null）
     */
    public static void checkUpgradeAchievements(UpgradeContainer container, ServerPlayer player) {
        if (player == null) {
            return;
        }

        int threadCount = 0;
        Set<String> threadLevels = new HashSet<>();
        boolean hasStorage = false;
        boolean hasThread = false;

        for (int i = 0; i < UpgradeContainer.SLOT_COUNT; i++) {
            ItemStack stack = container.getUpgradeStack(i);
            if (stack.isEmpty() || !(stack.getItem() instanceof UpgradeItem upgrade)) {
                continue;
            }
            UpgradeType type = upgrade.getDefinition().type();
            if (type == UpgradeType.THREAD) {
                hasThread = true;
                threadCount++;
                threadLevels.add(upgrade.getDefinition().level());
            } else if (type == UpgradeType.STORAGE) {
                hasStorage = true;
            }
        }

        // Pentium 4 HT：为任意机器安装任意多线程升级模块
        if (hasThread) {
            player.getAdvancements().award(player.server.getAdvancements().get(
                com.youyimc.createmassenergy.CreateMassenergy.rl("pentium4_ht")), "trigger");
        }

        // IBM 350：为机器安装任意存储升级模块
        if (hasStorage) {
            player.getAdvancements().award(player.server.getAdvancements().get(
                com.youyimc.createmassenergy.CreateMassenergy.rl("ibm350")), "trigger");
        }

        // 多路CPU：同一个机器装1个以上线程升级模块
        if (threadCount > 1) {
            player.getAdvancements().award(player.server.getAdvancements().get(
                com.youyimc.createmassenergy.CreateMassenergy.rl("multi_cpu")), "trigger");
        }

        // 华擎妖板：装不同等级的线程升级模块
        if (threadLevels.size() > 1) {
            player.getAdvancements().award(player.server.getAdvancements().get(
                com.youyimc.createmassenergy.CreateMassenergy.rl("asrock")), "trigger");
        }
    }

    public static void triggerFromBlockEntity(BlockEntity be, ServerPlayer player) {
        if (be instanceof UpgradeContainer container) {
            checkUpgradeAchievements(container, player);
        }
    }
}
