package com.youyimc.createmassenergy.util;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.annotation.Nullable;

import com.youyimc.createmassenergy.ModSounds;

import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredHolder;

/**
 * 收发报机节日特殊待机音效工具。
 * <p>
 * 维护"日期 → 节日特殊待机音效"的映射，并提供当前系统日期对应的节日查询。
 * 客户端用于在对应日期以 50% 概率用节日音效替代常规待机音；
 * 服务端用于在玩家首次听到时发放对应彩蛋成就。
 */
public final class TelegraphFestivals {

    /** 单个节日信息 */
    public record Festival(
            int month,
            int day,
            String key,                 // 唯一键（用于成就 resource location 后缀）
            DeferredHolder<SoundEvent, SoundEvent> sound,
            @Nullable String description) {
    }

    /** 月/日 → 节日（按月份顺序） */
    private static final Map<String, Festival> FESTIVALS = new LinkedHashMap<>();

    static {
        register(1, 7, "sacred_war", ModSounds.TELEGRAPH_FESTIVE_1_7, "1942年的今天，苏联红军战士们英勇无畏地捍卫了自己的祖国母亲，纳粹侵略者没能踏入莫斯科一步");
        register(2, 24, "proletarians_unite", ModSounds.TELEGRAPH_FESTIVE_2_24, "1848年的今天，一本叫做《共产党宣言》的著作在伦敦出版");
        register(3, 28, "merry_christmas_mr", ModSounds.TELEGRAPH_FESTIVE_3_28, "圣诞快乐，劳伦斯先生");
        register(7, 7, "defend_yellow_river", ModSounds.TELEGRAPH_FESTIVE_7_7, "日本军国主义的伤痕至今犹存");
        register(7, 28, "dmail", ModSounds.TELEGRAPH_FESTIVE_7_28, "2010年7月28日，冈部伦太郎欺骗了自己");
        register(9, 10, "field_fight", ModSounds.TELEGRAPH_FESTIVE_9_10, "Battlefield 1942发布");
        register(9, 11, "allende", ModSounds.TELEGRAPH_FESTIVE_9_11, "团结的人民永远不可阻挡");
        register(12, 25, "christmas", ModSounds.TELEGRAPH_FESTIVE_12_25, "God bless you, child");
        register(12, 26, "people_victory", ModSounds.TELEGRAPH_FESTIVE_12_26, "多年前的今天，一位伟人诞生");
    }

    private static void register(int month, int day, String key,
            DeferredHolder<SoundEvent, SoundEvent> sound, String description) {
        FESTIVALS.put(month + "_" + day, new Festival(month, day, key, sound, description));
    }

    private TelegraphFestivals() {}

    /**
     * 返回指定日期（月/日）对应的节日；若当天无节日返回 {@code null}。
     */
    @Nullable
    public static Festival getFestival(int month, int day) {
        return FESTIVALS.get(month + "_" + day);
    }

    /**
     * 返回今天（客户端系统日期）对应的节日；非节日返回 {@code null}。
     */
    @Nullable
    public static Festival getTodayFestival() {
        LocalDate today = LocalDate.now();
        return getFestival(today.getMonthValue(), today.getDayOfMonth());
    }

    /** 所有节日 */
    public static Iterable<Festival> all() {
        return FESTIVALS.values();
    }

    /** 是否存在节日 */
    public static boolean isFestivalToday() {
        return getTodayFestival() != null;
    }
}
