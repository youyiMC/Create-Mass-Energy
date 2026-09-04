package com.youyimc.createmassenergy.client.sound;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.youyimc.createmassenergy.ModSounds;
import com.youyimc.createmassenergy.block.entity.DataTerminalBlockEntity;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;

/**
 * 数据化终端工作循环音效管理（客户端）。
 * <p>
 * 为正在"真正处理数据"的数据化终端播放循环音效；因能量不足/存储满等原因
 * 暂停时不播放（服务器端以 {@code WORKING=false} 表示，音效随之停止）。
 * 通过 {@link net.neoforged.neoforge.client.event.RenderLevelStageEvent} 每帧驱动。
 * <p>
 * 音量使用手动距离衰减（平滑渐弱），替代引擎的硬截断。
 */
public final class DataTerminalSoundHandler {

    private static final Map<BlockPos, TerminalLoopSound> ACTIVE_SOUNDS = new HashMap<>();
    /** 探测范围（半边长，格） */
    private static final int SCAN_RANGE = 24;
    /** 音效最远可闻距离（格）——超过则停止，范围内平滑衰减 */
    private static final int MAX_RANGE = 40;
    /** 近处满音量距离（格） */
    private static final double FULL_VOLUME_DIST = 6.0;
    /** 基础音量 */
    private static final float BASE_VOLUME = 0.8f;

    private DataTerminalSoundHandler() {}

    /**
     * 每帧调用：扫描附近数据化终端，管理循环音效。
     */
    public static void tick() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) {
            stopAll(mc.getSoundManager());
            return;
        }

        SoundManager soundManager = mc.getSoundManager();
        Vec3 playerPos = mc.player.position();
        BlockPos playerBlock = mc.player.blockPosition();

        // 1. 查找探测范围内真正工作的数据化终端
        Map<BlockPos, DataTerminalBlockEntity> workingTerminals = new HashMap<>();
        for (int dx = -SCAN_RANGE; dx <= SCAN_RANGE; dx++) {
            for (int dy = -SCAN_RANGE; dy <= SCAN_RANGE; dy++) {
                for (int dz = -SCAN_RANGE; dz <= SCAN_RANGE; dz++) {
                    BlockPos pos = playerBlock.offset(dx, dy, dz);
                    BlockEntity be = mc.level.getBlockEntity(pos);
                    if (be instanceof DataTerminalBlockEntity terminal && terminal.isWorking()) {
                        workingTerminals.put(pos, terminal);
                    }
                }
            }
        }

        // 2. 为工作中的终端启动/维持循环音效（是否可闻由各音源按距离自行衰减）
        for (BlockPos pos : workingTerminals.keySet()) {
            if (ACTIVE_SOUNDS.containsKey(pos)) {
                continue; // 已在播放
            }
            if (playerPos.distanceToSqr(Vec3.atCenterOf(pos)) > MAX_RANGE * MAX_RANGE) {
                continue;
            }
            TerminalLoopSound sound = new TerminalLoopSound(pos);
            ACTIVE_SOUNDS.put(pos, sound);
            soundManager.play(sound);
        }

        // 3. 停止不再工作/超出最远距离的循环音效
        Iterator<Map.Entry<BlockPos, TerminalLoopSound>> it = ACTIVE_SOUNDS.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<BlockPos, TerminalLoopSound> entry = it.next();
            BlockPos pos = entry.getKey();
            TerminalLoopSound sound = entry.getValue();
            boolean stillWorking = workingTerminals.containsKey(pos);
            boolean inRange = playerPos.distanceToSqr(Vec3.atCenterOf(pos)) <= MAX_RANGE * MAX_RANGE;
            if (!stillWorking || !inRange) {
                soundManager.stop(sound);
                it.remove();
            }
        }
    }

    /** 停止所有循环音效（世界卸载等场景） */
    public static void stopAll(SoundManager soundManager) {
        for (TerminalLoopSound sound : ACTIVE_SOUNDS.values()) {
            soundManager.stop(sound);
        }
        ACTIVE_SOUNDS.clear();
    }

    /** 循环音效实例（手动距离衰减） */
    private static class TerminalLoopSound extends AbstractTickableSoundInstance {

        private final BlockPos pos;

        protected TerminalLoopSound(BlockPos pos) {
            super(ModSounds.DATA_TERMINAL_WORKING.get(), SoundSource.BLOCKS, RandomSource.create());
            this.pos = pos;
            this.looping = true;
            this.delay = 0;
            this.volume = 0f; // 初始静音，tick 内按距离设定，避免启动瞬间爆音
            this.pitch = 1.0f;
            this.x = pos.getX() + 0.5;
            this.y = pos.getY() + 0.5;
            this.z = pos.getZ() + 0.5;
            // 关闭引擎距离衰减，改用 tick 内手动平滑衰减
            this.attenuation = Attenuation.NONE;
            this.relative = false;
        }

        @Override
        public void tick() {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null) {
                return;
            }
            double dist = mc.player.distanceToSqr(this.x, this.y, this.z);
            if (dist > MAX_RANGE * MAX_RANGE) {
                // 超远：交由外部管理器停止；此处静音作为兜底
                this.volume = 0f;
                return;
            }
            double d = Math.sqrt(dist);
            // 平滑衰减：FULL_VOLUME_DIST 内满音量，之后线性降至 0
            float v;
            if (d <= FULL_VOLUME_DIST) {
                v = BASE_VOLUME;
            } else {
                double t = 1.0 - (d - FULL_VOLUME_DIST) / (MAX_RANGE - FULL_VOLUME_DIST);
                v = (float) (BASE_VOLUME * Math.max(0.0, Math.min(1.0, t)));
            }
            this.volume = v;
        }

        @Override
        public boolean canStartSilent() {
            return true;
        }
    }
}
