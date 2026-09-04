package com.youyimc.createmassenergy.thread;

import net.minecraft.world.item.ItemStack;

/**
 * 一个"作业"表示对某种物品的连续处理任务。
 * <p>
 * 线程规则（文档）：
 * <ul>
 *   <li>同种物品的作业固定占用一个线程</li>
 *   <li>不同物品占用不同线程</li>
 *   <li>若机器存在空闲线程，则可将同种物品拆分为多个作业并行处理</li>
 * </ul>
 * 这里用 processingPerTick 记录每个作业当前每秒处理数量（用于拆分）。
 */
public class Job {

    /** 物品类型（用于区分"同种物品"） */
    private final ItemStack item;
    /** 当前线程分配到的每秒处理量 */
    private double processPerTick;
    /** 累积的进度（小数部分） */
    private double progress;

    public Job(ItemStack item, double processPerTick) {
        this.item = item.copy();
        this.item.setCount(1);
        this.processPerTick = processPerTick;
        this.progress = 0;
    }

    public ItemStack getItem() {
        return item;
    }

    public double getProcessPerTick() {
        return processPerTick;
    }

    public void setProcessPerTick(double processPerTick) {
        this.processPerTick = processPerTick;
    }

    /**
     * 每 tick 推进处理，返回本 tick 实际处理的物品数（可能为 0 或多个）
     */
    public int tick(double speedFactor) {
        progress += processPerTick * speedFactor;
        if (progress >= 1.0) {
            int processed = (int) progress;
            progress -= processed;
            return processed;
        }
        return 0;
    }

    /**
     * 判断两个作业是否处理同种物品
     */
    public boolean sameItem(Job other) {
        return ItemStack.isSameItemSameComponents(item, other.item);
    }
}
