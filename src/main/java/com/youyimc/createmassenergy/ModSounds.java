package com.youyimc.createmassenergy;

import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * 音效注册
 */
public final class ModSounds {
    private ModSounds() {}

    public static final DeferredRegister<SoundEvent> SOUNDS =
        DeferredRegister.create(Registries.SOUND_EVENT, CreateMassenergy.MODID);

    /** 数据化终端工作音效 */
    public static final DeferredHolder<SoundEvent, SoundEvent> DATA_TERMINAL_WORKING =
        SOUNDS.register("data_terminal_working",
            () -> SoundEvent.createVariableRangeEvent(CreateMassenergy.rl("data_terminal_working")));

    // ==================== 收发报机音效 ====================

    /** 数据发送中（每 5 秒一声的事件音） */
    public static final DeferredHolder<SoundEvent, SoundEvent> TELEGRAPH_SEND =
        SOUNDS.register("telegraph_send",
            () -> SoundEvent.createVariableRangeEvent(CreateMassenergy.rl("telegraph_send")));

    /** 数据接收中（每 5 秒一声的事件音） */
    public static final DeferredHolder<SoundEvent, SoundEvent> TELEGRAPH_RECEIVE =
        SOUNDS.register("telegraph_receive",
            () -> SoundEvent.createVariableRangeEvent(CreateMassenergy.rl("telegraph_receive")));

    /** 待机音效（含 3 个文件，播放时随机） */
    public static final DeferredHolder<SoundEvent, SoundEvent> TELEGRAPH_IDLE =
        SOUNDS.register("telegraph_idle",
            () -> SoundEvent.createVariableRangeEvent(CreateMassenergy.rl("telegraph_idle")));

    /** 节日特殊待机音效（每个节日日期一个 event） */
    public static final DeferredHolder<SoundEvent, SoundEvent> TELEGRAPH_FESTIVE_1_7 =
        SOUNDS.register("telegraph_festive_1_7",
            () -> SoundEvent.createVariableRangeEvent(CreateMassenergy.rl("telegraph_festive_1_7")));
    public static final DeferredHolder<SoundEvent, SoundEvent> TELEGRAPH_FESTIVE_2_24 =
        SOUNDS.register("telegraph_festive_2_24",
            () -> SoundEvent.createVariableRangeEvent(CreateMassenergy.rl("telegraph_festive_2_24")));
    public static final DeferredHolder<SoundEvent, SoundEvent> TELEGRAPH_FESTIVE_3_28 =
        SOUNDS.register("telegraph_festive_3_28",
            () -> SoundEvent.createVariableRangeEvent(CreateMassenergy.rl("telegraph_festive_3_28")));
    public static final DeferredHolder<SoundEvent, SoundEvent> TELEGRAPH_FESTIVE_7_7 =
        SOUNDS.register("telegraph_festive_7_7",
            () -> SoundEvent.createVariableRangeEvent(CreateMassenergy.rl("telegraph_festive_7_7")));
    public static final DeferredHolder<SoundEvent, SoundEvent> TELEGRAPH_FESTIVE_7_28 =
        SOUNDS.register("telegraph_festive_7_28",
            () -> SoundEvent.createVariableRangeEvent(CreateMassenergy.rl("telegraph_festive_7_28")));
    public static final DeferredHolder<SoundEvent, SoundEvent> TELEGRAPH_FESTIVE_9_10 =
        SOUNDS.register("telegraph_festive_9_10",
            () -> SoundEvent.createVariableRangeEvent(CreateMassenergy.rl("telegraph_festive_9_10")));
    public static final DeferredHolder<SoundEvent, SoundEvent> TELEGRAPH_FESTIVE_9_11 =
        SOUNDS.register("telegraph_festive_9_11",
            () -> SoundEvent.createVariableRangeEvent(CreateMassenergy.rl("telegraph_festive_9_11")));
    public static final DeferredHolder<SoundEvent, SoundEvent> TELEGRAPH_FESTIVE_12_25 =
        SOUNDS.register("telegraph_festive_12_25",
            () -> SoundEvent.createVariableRangeEvent(CreateMassenergy.rl("telegraph_festive_12_25")));
    public static final DeferredHolder<SoundEvent, SoundEvent> TELEGRAPH_FESTIVE_12_26 =
        SOUNDS.register("telegraph_festive_12_26",
            () -> SoundEvent.createVariableRangeEvent(CreateMassenergy.rl("telegraph_festive_12_26")));
}
