package com.youyimc.createmassenergy.network;

import java.util.UUID;

import com.youyimc.createmassenergy.block.entity.RadioTelegraphBlockEntity;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * 收发报机配对切换包（客户端 → 服务器）
 */
public record RadioTelegraphPairPayload(BlockPos pos, UUID receiverId) implements CustomPacketPayload {

    public static final Type<RadioTelegraphPairPayload> TYPE =
        new Type<>(ModNetworking.rl("radio_telegraph_pair"));

    /** 手动实现编解码（UUID 需要特殊处理） */
    public static final StreamCodec<ByteBuf, RadioTelegraphPairPayload> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public RadioTelegraphPairPayload decode(ByteBuf buffer) {
            BlockPos pos = BlockPos.STREAM_CODEC.decode(buffer);
            long most = buffer.readLong();
            long least = buffer.readLong();
            return new RadioTelegraphPairPayload(pos, new UUID(most, least));
        }

        @Override
        public void encode(ByteBuf buffer, RadioTelegraphPairPayload payload) {
            BlockPos.STREAM_CODEC.encode(buffer, payload.pos());
            buffer.writeLong(payload.receiverId().getMostSignificantBits());
            buffer.writeLong(payload.receiverId().getLeastSignificantBits());
        }
    };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(RadioTelegraphPairPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer serverPlayer) {
                BlockEntity be = serverPlayer.serverLevel().getBlockEntity(payload.pos());
                if (be instanceof RadioTelegraphBlockEntity radio) {
                    if (radio.getMode() == RadioTelegraphBlockEntity.Mode.SEND) {
                        radio.togglePair(payload.receiverId());
                    }
                }
            }
        });
    }
}
