package com.youyimc.createmassenergy.network;

import com.youyimc.createmassenergy.block.entity.RadioTelegraphBlockEntity;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * 收发报机模式切换包（客户端 → 服务器）
 */
public record RadioTelegraphModePayload(BlockPos pos, boolean receive) implements CustomPacketPayload {

    public static final Type<RadioTelegraphModePayload> TYPE =
        new Type<>(ModNetworking.rl("radio_telegraph_mode"));

    public static final StreamCodec<ByteBuf, RadioTelegraphModePayload> STREAM_CODEC = StreamCodec.composite(
        BlockPos.STREAM_CODEC, RadioTelegraphModePayload::pos,
        ByteBufCodecs.BOOL, RadioTelegraphModePayload::receive,
        RadioTelegraphModePayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(RadioTelegraphModePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer serverPlayer) {
                BlockEntity be = serverPlayer.serverLevel().getBlockEntity(payload.pos());
                if (be instanceof RadioTelegraphBlockEntity radio) {
                    radio.setMode(payload.receive()
                        ? RadioTelegraphBlockEntity.Mode.RECEIVE
                        : RadioTelegraphBlockEntity.Mode.SEND);
                }
            }
        });
    }
}
