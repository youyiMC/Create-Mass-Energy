package com.youyimc.createmassenergy.network;

import java.util.List;
import java.util.UUID;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * 收发报机可用接收器列表响应包（服务器 → 客户端）。
 * <p>
 * 响应客户端的 {@link RadioTelegraphRequestReceiversPayload} 请求，
 * 携带全服可用接收器及当前配对状态，供 GUI 配对列表渲染。
 */
public record RadioTelegraphReceiversPayload(BlockPos pos, List<ReceiverInfo> receivers)
        implements CustomPacketPayload {

    /** 单个接收器信息 */
    public record ReceiverInfo(UUID id, String name, boolean paired) {}

    public static final Type<RadioTelegraphReceiversPayload> TYPE =
        new Type<>(ModNetworking.rl("radio_telegraph_receivers"));

    /** UUID 编解码（1.21.1 无内置 UUID codec，手动读写两个 long） */
    private static final StreamCodec<ByteBuf, UUID> UUID_CODEC = new StreamCodec<>() {
        @Override
        public UUID decode(ByteBuf buffer) {
            return new UUID(buffer.readLong(), buffer.readLong());
        }

        @Override
        public void encode(ByteBuf buffer, UUID value) {
            buffer.writeLong(value.getMostSignificantBits());
            buffer.writeLong(value.getLeastSignificantBits());
        }
    };

    private static final StreamCodec<ByteBuf, ReceiverInfo> RECEIVER_CODEC = StreamCodec.composite(
        UUID_CODEC, ReceiverInfo::id,
        ByteBufCodecs.STRING_UTF8, ReceiverInfo::name,
        ByteBufCodecs.BOOL, ReceiverInfo::paired,
        ReceiverInfo::new);

    public static final StreamCodec<ByteBuf, RadioTelegraphReceiversPayload> STREAM_CODEC = StreamCodec.composite(
        BlockPos.STREAM_CODEC, RadioTelegraphReceiversPayload::pos,
        RECEIVER_CODEC.apply(ByteBufCodecs.list()), RadioTelegraphReceiversPayload::receivers,
        RadioTelegraphReceiversPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    /** 客户端处理：更新 GUI 中的配对列表数据 */
    public static void handle(RadioTelegraphReceiversPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            // 将数据交给当前打开的收发报机屏幕（如果对应）
            com.youyimc.createmassenergy.client.gui.RadioTelegraphScreen.receiveReceivers(payload);
        });
    }
}
