package com.youyimc.createmassenergy.network;

import com.youyimc.createmassenergy.block.entity.RadioTelegraphBlockEntity;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * 收发报机接收器列表请求包（客户端 → 服务器）。
 * <p>
 * 客户端打开收发报机 GUI（发送模式）时发送，服务端查询全服可用接收器并回复
 * {@link RadioTelegraphReceiversPayload}。
 */
public record RadioTelegraphRequestReceiversPayload(BlockPos pos) implements CustomPacketPayload {

    public static final Type<RadioTelegraphRequestReceiversPayload> TYPE =
        new Type<>(ModNetworking.rl("radio_telegraph_request_receivers"));

    public static final StreamCodec<ByteBuf, RadioTelegraphRequestReceiversPayload> STREAM_CODEC = StreamCodec.composite(
        BlockPos.STREAM_CODEC, RadioTelegraphRequestReceiversPayload::pos,
        RadioTelegraphRequestReceiversPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(RadioTelegraphRequestReceiversPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer serverPlayer) {
                BlockEntity be = serverPlayer.serverLevel().getBlockEntity(payload.pos());
                if (be instanceof RadioTelegraphBlockEntity radio) {
                    // 收集当前配对（发送模式下的接收器 UUID）
                    java.util.Set<java.util.UUID> paired =
                        new java.util.HashSet<>(radio.getPairedReceivers());
                    // 查询全服可用接收器
                    java.util.List<RadioTelegraphReceiversPayload.ReceiverInfo> receivers =
                        new java.util.ArrayList<>();
                    for (com.youyimc.createmassenergy.radio.RadioRegistry.Entry entry
                            : com.youyimc.createmassenergy.radio.RadioRegistry.getReceivers()) {
                        if (entry.id.equals(radio.getRadioId())) {
                            continue; // 排除自己
                        }
                        receivers.add(new RadioTelegraphReceiversPayload.ReceiverInfo(
                            entry.id, entry.name, paired.contains(entry.id)));
                    }
                    PacketDistributor.sendToPlayer(serverPlayer,
                        new RadioTelegraphReceiversPayload(payload.pos(), receivers));
                }
            }
        });
    }
}
