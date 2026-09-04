package com.youyimc.createmassenergy.thread;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

import net.minecraft.world.item.ItemStack;

/**
 * 线程调度管理器。
 * <p>
 * 维护一组作业（每个作业绑定一种物品），根据机器可用的线程数动态分配处理能力。
 * 文档要求：
 * <ul>
 *   <li>同种物品固定占用一个线程；不同物品占用不同线程</li>
 *   <li>若机器有空闲线程，可将同种物品拆分为多个作业并行处理以提升吞吐量</li>
 * </ul>
 * 实现简化：每个物品类型对应一个作业；若线程数大于物品类型数，则从最快被消费完的
 * 作业中拆分出额外作业（每拆分一个作业多一份处理能力）。
 * 由于 Java 单线程 tick 模型，这里"线程"体现为处理能力的叠加，而不是真正的并发。
 */
public class ThreadManager {

    private final List<Job> jobs = new ArrayList<>();
    private int availableThreads = 1;
    private double speedFactor = 1.0;

    /**
     * 处理回调：onProcessed(item, count) 在每 tick 消费物品时调用
     */
    private final BiConsumer<ItemStack, Integer> onProcessed;

    public ThreadManager(BiConsumer<ItemStack, Integer> onProcessed) {
        this.onProcessed = onProcessed;
    }

    /**
     * 设置可用线程数（由升级模块决定）
     */
    public void setAvailableThreads(int threads) {
        this.availableThreads = Math.max(1, threads);
    }

    public int getAvailableThreads() {
        return availableThreads;
    }

    public void setSpeedFactor(double speedFactor) {
        this.speedFactor = speedFactor;
    }

    /**
     * 查询当前是否有处理指定物品的作业
     */
    public boolean hasJobFor(ItemStack item) {
        for (Job job : jobs) {
            if (ItemStack.isSameItemSameComponents(job.getItem(), item)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 为指定物品创建作业（如果不存在）
     */
    public void ensureJobFor(ItemStack item) {
        if (!hasJobFor(item)) {
            jobs.add(new Job(item, 1.0));
            redistribute();
        }
    }

    /**
     * 移除指定物品的作业（当库存中没有该物品时）
     */
    public void removeJobFor(ItemStack item) {
        jobs.removeIf(job -> ItemStack.isSameItemSameComponents(job.getItem(), item));
        redistribute();
    }

    /**
     * 每 tick 推进所有作业。
     * <p>
     * 注意：onProcessed 回调中可能通过方块实体修改 jobs 列表（如物品耗尽触发
     * ensureJobFor/cleanup/removeJobFor），因此这里迭代 jobs 的防御性拷贝，
     * 避免 ConcurrentModificationException。
     */
    public void tick() {
        List<Job> snapshot = new ArrayList<>(jobs);
        for (Job job : snapshot) {
            int processed = job.tick(speedFactor);
            if (processed > 0) {
                onProcessed.accept(job.getItem(), processed);
            }
        }
    }

    /**
     * 重新分配线程能力。
     * 基本规则：每种物品至少一个作业（占1线程），每多一个线程可为某物品多拆分一个作业。
     */
    private void redistribute() {
        if (jobs.isEmpty()) {
            return;
        }
        // 每种物品基础 1 线程
        int baseThreads = jobs.size();
        // 额外线程用于拆分
        int extraThreads = Math.max(0, availableThreads - baseThreads);

        // 重置所有作业为基础 1 单位
        for (Job job : jobs) {
            job.setProcessPerTick(1.0);
        }

        // 将额外线程均匀分配到各作业（尽量让处理中的作业受益）
        // 每个作业获得 ceil(extra / n) 的额外能力，避免产生太多零散作业
        int n = jobs.size();
        int totalExtra = extraThreads;
        // 简单轮询分配：把额外线程逐个加给每个作业（这样同种物品被拆分）
        int idx = 0;
        while (totalExtra > 0) {
            Job job = jobs.get(idx % n);
            job.setProcessPerTick(job.getProcessPerTick() + 1.0);
            totalExtra--;
            idx++;
        }
    }

    /**
     * 重新分配（供外部在物品/线程变化时调用）
     */
    public void onChanged() {
        redistribute();
    }

    /**
     * 当前活跃作业数
     */
    public int getJobCount() {
        return jobs.size();
    }

    public boolean isEmpty() {
        return jobs.isEmpty();
    }

    /**
     * 获取所有作业的副本
     */
    public List<Job> getJobs() {
        return new ArrayList<>(jobs);
    }

    /**
     * 清理：删除已经没有可用库存的作业
     */
    public void cleanup(List<ItemStack> availableStacks) {
        jobs.removeIf(job -> {
            for (ItemStack stack : availableStacks) {
                if (ItemStack.isSameItemSameComponents(stack, job.getItem())) {
                    return false;
                }
            }
            return true;
        });
        redistribute();
    }
}
