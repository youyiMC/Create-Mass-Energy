package com.youyimc.createmassenergy.radio;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * 全服收发报机注册表（服务器端）。
 * <p>
 * 记录所有已命名的收发报机：
 * <ul>
 *   <li>接收器列表：全服所有处于"接收"模式且有名字的收发报机</li>
 *   <li>发射器列表：全服所有处于"发送"模式且有名字的收发报机</li>
 * </ul>
 * <p>
 * 航空学联动：每个条目保存方块实体的弱引用，使收发报机位于航空学物理结构
 * （sub-level）时也能跨结构被访问（弱引用不依赖坐标，天然支持跨 sub-level）。
 */
public class RadioRegistry {

    /** 每个收发报机的唯一标识（使用方块实体 UUID） */
    public static class Entry {
        public final UUID id;
        public ServerLevel level;
        public BlockPos pos;
        public String name;
        public boolean isSender;
        /** 发送模式下：配对的接收器 id 列表（按优先级排序） */
        public final Set<UUID> pairedReceivers = new HashSet<>();
        /** 方块实体弱引用（航空学联动：跨 sub-level 访问的关键） */
        private WeakReference<BlockEntity> beRef;

        public Entry(UUID id, ServerLevel level, BlockPos pos, String name, boolean isSender) {
            this.id = id;
            this.level = level;
            this.pos = pos;
            this.name = name;
            this.isSender = isSender;
        }

        /** 更新方块实体弱引用 */
        public void setBlockEntity(BlockEntity be) {
            this.beRef = be != null ? new WeakReference<>(be) : null;
        }

        /**
         * 获取方块实体。
         * <p>
         * 优先使用弱引用（跨 sub-level 有效），弱引用失效时回退到 level+pos 查询。
         */
        public BlockEntity getBlockEntity() {
            if (beRef != null) {
                BlockEntity be = beRef.get();
                if (be != null && !be.isRemoved()) {
                    return be;
                }
                beRef = null;
            }
            if (level != null && !level.isClientSide && pos != null) {
                BlockEntity be = level.getBlockEntity(pos);
                if (be != null && !be.isRemoved()) {
                    return be;
                }
            }
            return null;
        }
    }

    private static final Map<UUID, Entry> ENTRIES = new HashMap<>();

    private RadioRegistry() {}

    /** 注册或更新一个收发报机 */
    public static void register(UUID id, ServerLevel level, BlockPos pos, String name, boolean isSender, BlockEntity be) {
        Entry entry = ENTRIES.computeIfAbsent(id, k -> new Entry(id, level, pos, name, isSender));
        entry.level = level;
        entry.pos = pos;
        entry.name = name;
        entry.isSender = isSender;
        entry.setBlockEntity(be);
    }

    /** 移除一个收发报机 */
    public static void unregister(UUID id) {
        ENTRIES.remove(id);
    }

    /** 获取指定收发报机条目 */
    public static Entry get(UUID id) {
        return ENTRIES.get(id);
    }

    /** 获取全服所有命名接收器 */
    public static Set<Entry> getReceivers() {
        Set<Entry> result = new HashSet<>();
        for (Entry entry : ENTRIES.values()) {
            if (!entry.isSender && entry.name != null && !entry.name.isBlank()) {
                result.add(entry);
            }
        }
        return result;
    }

    /** 获取全服所有命名发射器 */
    public static Set<Entry> getSenders() {
        Set<Entry> result = new HashSet<>();
        for (Entry entry : ENTRIES.values()) {
            if (entry.isSender && entry.name != null && !entry.name.isBlank()) {
                result.add(entry);
            }
        }
        return result;
    }

    /** 获取所有注册条目（用于 GUI 显示） */
    public static Set<Entry> getAll() {
        return new HashSet<>(ENTRIES.values());
    }

    /** 判断指定位置（world,pos）是否有接收器（用于客户端渲染提示） */
    public static boolean isReceiverAt(ServerLevel level, BlockPos pos) {
        for (Entry entry : ENTRIES.values()) {
            if (!entry.isSender && entry.level == level && entry.pos.equals(pos)) {
                return true;
            }
        }
        return false;
    }

    /** 服务器世界卸载时清理该世界条目 */
    public static void unloadLevel(Level level) {
        ENTRIES.values().removeIf(entry -> entry.level == level);
    }
}
