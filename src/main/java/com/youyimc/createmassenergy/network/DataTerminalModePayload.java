package com.youyimc.createmassenergy.network;

import com.youyimc.createmassenergy.block.entity.DataTerminalBlockEntity;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * 数据化终端模式切换包（客户端 → 服务器）
 */
public record DataTerminalModePayload(BlockPos pos, boolean restore) implements CustomPacketPayload {

    public static final Type<DataTerminalModePayload> TYPE =
        new Type<>(ModNetworking.rl("data_terminal_mode"));

    public static final StreamCodec<ByteBuf, DataTerminalModePayload> STREAM_CODEC = StreamCodec.composite(
        BlockPos.STREAM_CODEC, DataTerminalModePayload::pos,
        ByteBufCodecs.BOOL, DataTerminalModePayload::restore,
        DataTerminalModePayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(DataTerminalModePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer serverPlayer) {
                BlockEntity be = serverPlayer.serverLevel().getBlockEntity(payload.pos());
                if (be instanceof DataTerminalBlockEntity terminal) {
                    terminal.setMode(payload.restore()
                        ? DataTerminalBlockEntity.Mode.RESTORE
                        : DataTerminalBlockEntity.Mode.DECOMPOSE);
                }
            }
        });
    }
}
