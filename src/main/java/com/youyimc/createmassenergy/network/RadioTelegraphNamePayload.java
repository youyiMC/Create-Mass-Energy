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
 * 收发报机命名包（客户端 → 服务器）
 */
public record RadioTelegraphNamePayload(BlockPos pos, String name) implements CustomPacketPayload {

    public static final Type<RadioTelegraphNamePayload> TYPE =
        new Type<>(ModNetworking.rl("radio_telegraph_name"));

    public static final StreamCodec<ByteBuf, RadioTelegraphNamePayload> STREAM_CODEC = StreamCodec.composite(
        BlockPos.STREAM_CODEC, RadioTelegraphNamePayload::pos,
        ByteBufCodecs.STRING_UTF8, RadioTelegraphNamePayload::name,
        RadioTelegraphNamePayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(RadioTelegraphNamePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer serverPlayer) {
                BlockEntity be = serverPlayer.serverLevel().getBlockEntity(payload.pos());
                if (be instanceof RadioTelegraphBlockEntity radio) {
                    // 限制名字长度防止滥用
                    String name = payload.name() == null ? "" : payload.name();
                    if (name.length() > 32) {
                        name = name.substring(0, 32);
                    }
                    radio.setRadioName(name);
                }
            }
        });
    }
}
