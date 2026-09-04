package com.youyimc.createmassenergy.network;

import com.youyimc.createmassenergy.CreateMassenergy;
import com.youyimc.createmassenergy.util.TelegraphFestivals;

import io.netty.buffer.ByteBuf;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * 收发报机节日音效成就上报包（客户端 → 服务器）。
 * <p>
 * 当玩家听到（16 格内）某个节日特殊待机音效时发送，服务端发放对应彩蛋成就。
 * 携带节日 key（{@code 1_7}、{@code 12_25} 等）以及固定总成就 key 由服务端处理。
 */
public record RadioTelegraphFestivalPayload(String festivalKey) implements CustomPacketPayload {

    public static final Type<RadioTelegraphFestivalPayload> TYPE =
        new Type<>(ModNetworking.rl("radio_telegraph_festival"));

    public static final StreamCodec<ByteBuf, RadioTelegraphFestivalPayload> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.STRING_UTF8, RadioTelegraphFestivalPayload::festivalKey,
        RadioTelegraphFestivalPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(RadioTelegraphFestivalPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer serverPlayer) {
                // 总成就：跨次元通讯（听到任意特殊待机音效）
                serverPlayer.getAdvancements().award(
                    serverPlayer.server.getAdvancements().get(
                        CreateMassenergy.rl("cross_dimensional_communication")), "trigger");
                // 具体节日成就（以 festival key 结尾的成就 id）
                String key = payload.festivalKey();
                if (key != null && !key.isBlank()) {
                    // festival key 如 1_7 / 12_25；成就 id 前缀 fest_
                    serverPlayer.getAdvancements().award(
                        serverPlayer.server.getAdvancements().get(
                            CreateMassenergy.rl("fest_" + key)), "trigger");
                }
                // 集齐所有彩蛋电报音成就后发放：永不消逝的电波
                checkEternalWave(serverPlayer);
            }
        });
    }

    /** 若玩家已集齐全部 9 个节日彩蛋成就，则发放“永不消逝的电波” */
    private static void checkEternalWave(ServerPlayer player) {
        var manager = player.server.getAdvancements();
        for (TelegraphFestivals.Festival f : TelegraphFestivals.all()) {
            AdvancementHolder holder = manager.get(CreateMassenergy.rl("fest_" + f.key()));
            if (holder == null || !player.getAdvancements().getOrStartProgress(holder).isDone()) {
                return; // 还有未完成的节日成就
            }
        }
        player.getAdvancements().award(manager.get(
            CreateMassenergy.rl("eternal_wave")), "trigger");
    }
}
