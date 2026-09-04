package com.youyimc.createmassenergy.client.sound;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.youyimc.createmassenergy.ModSounds;
import com.youyimc.createmassenergy.block.entity.RadioTelegraphBlockEntity;
import com.youyimc.createmassenergy.network.RadioTelegraphFestivalPayload;
import com.youyimc.createmassenergy.util.TelegraphFestivals;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * 收发报机音效管理（客户端）。
 * <p>
 * 由 {@link net.neoforged.neoforge.client.event.RenderLevelStageEvent} 每帧驱动。
 * <ul>
 *   <li>发送/接收音效：电报机"真正在收发数据"时，每 5 秒播放一声（按模式区分）</li>
 *   <li>待机音效：有名字且空闲时，每 30 秒播放一次；若当天是节日，有 50% 概率用
 *       节日特殊待机音效替代常规待机音</li>
 *   <li>播放节日音效时，若玩家位于该电报机 16 格内，向服务端上报以触发彩蛋成就</li>
 * </ul>
 */
public final class RadioTelegraphSoundHandler {

    /** 探测范围（半边长，格） */
    private static final int SCAN_RANGE = 32;
    /** 传输音间隔（毫秒） */
    private static final long TRANSMIT_INTERVAL_MS = 5_000;
    /** 待机音间隔（毫秒） */
    private static final long IDLE_INTERVAL_MS = 30_000;
    /** 节日音替代概率 */
    private static final double FESTIVE_CHANCE = 0.5;
    /** 触发成就的半径（格） */
    private static final int ADVANCEMENT_RANGE = 16;

    /** 每个电报机的音效计时状态 */
    private static final Map<BlockPos, Entry> ENTRIES = new HashMap<>();

    private RadioTelegraphSoundHandler() {}

    /** 单个电报机的音效状态 */
    private static class Entry {
        long lastTransmitPlay = 0;
        long lastIdlePlay = 0;
    }

    /** 每帧调用 */
    public static void tick() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) {
            ENTRIES.clear();
            return;
        }
        long now = System.currentTimeMillis();
        BlockPos playerBlock = mc.player.blockPosition();

        // 扫描附近电报机
        Map<BlockPos, RadioTelegraphBlockEntity> found = new HashMap<>();
        for (int dx = -SCAN_RANGE; dx <= SCAN_RANGE; dx++) {
            for (int dy = -SCAN_RANGE; dy <= SCAN_RANGE; dy++) {
                for (int dz = -SCAN_RANGE; dz <= SCAN_RANGE; dz++) {
                    BlockPos pos = playerBlock.offset(dx, dy, dz);
                    BlockEntity be = mc.level.getBlockEntity(pos);
                    if (be instanceof RadioTelegraphBlockEntity rt
                            && rt.getRadioName() != null && !rt.getRadioName().isBlank()) {
                        found.put(pos, rt);
                    }
                }
            }
        }

        // 处理每个电报机
        for (Map.Entry<BlockPos, RadioTelegraphBlockEntity> e : found.entrySet()) {
            BlockPos pos = e.getKey();
            RadioTelegraphBlockEntity rt = e.getValue();
            Entry entry = ENTRIES.computeIfAbsent(pos, p -> new Entry());
            boolean transmitting = rt.isWorking();
            boolean modeSend = rt.getMode() == RadioTelegraphBlockEntity.Mode.SEND;

            if (transmitting) {
                // 传输音：每 5 秒一声
                if (now - entry.lastTransmitPlay >= TRANSMIT_INTERVAL_MS) {
                    SoundEvent ev = modeSend ? ModSounds.TELEGRAPH_SEND.get() : ModSounds.TELEGRAPH_RECEIVE.get();
                    playAt(mc, pos, ev, 0.9f);
                    entry.lastTransmitPlay = now;
                }
            } else {
                // 待机音：每 30 秒一次
                if (now - entry.lastIdlePlay >= IDLE_INTERVAL_MS) {
                    playIdle(mc, pos, rt, now);
                    entry.lastIdlePlay = now;
                }
            }
        }

        // 清理离开范围的记录
        Iterator<Map.Entry<BlockPos, Entry>> it = ENTRIES.entrySet().iterator();
        while (it.hasNext()) {
            if (!found.containsKey(it.next().getKey())) {
                it.remove();
            }
        }
    }

    /** 播放常规或节日待机音 */
    private static void playIdle(Minecraft mc, BlockPos pos, RadioTelegraphBlockEntity rt, long now) {
        TelegraphFestivals.Festival festival = TelegraphFestivals.getTodayFestival();
        boolean playFestive = festival != null && mc.level.random.nextDouble() < FESTIVE_CHANCE;
        if (playFestive) {
            playAt(mc, pos, festival.sound().get(), 1.0f);
            // 玩家 16 格内听到 → 上报成就
            if (mc.player.distanceToSqr(Vec3.atCenterOf(pos)) <= ADVANCEMENT_RANGE * ADVANCEMENT_RANGE) {
                PacketDistributor.sendToServer(new RadioTelegraphFestivalPayload(festival.key()));
            }
        } else {
            playAt(mc, pos, ModSounds.TELEGRAPH_IDLE.get(), 0.9f);
        }
    }

    /** 在方块位置播放音效（引擎距离衰减） */
    private static void playAt(Minecraft mc, BlockPos pos, SoundEvent event, float volume) {
        mc.level.playSound(mc.player, pos, event, SoundSource.BLOCKS, volume, 1.0f);
    }
}
