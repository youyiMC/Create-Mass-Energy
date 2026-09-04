package com.youyimc.createmassenergy.network;

import com.youyimc.createmassenergy.CreateMassenergy;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/**
 * 网络包注册
 */
public final class ModNetworking {

    private static final String PROTOCOL_VERSION = "1";

    private ModNetworking() {}

    /** 在 mod 构造时调用注册 */
    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(ModNetworking::registerPayloads);
    }

    private static void registerPayloads(RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(CreateMassenergy.MODID).versioned(PROTOCOL_VERSION);

        // 终端模式切换
        registrar.playToServer(DataTerminalModePayload.TYPE, DataTerminalModePayload.STREAM_CODEC,
            DataTerminalModePayload::handle);
        // 收发报机模式切换
        registrar.playToServer(RadioTelegraphModePayload.TYPE, RadioTelegraphModePayload.STREAM_CODEC,
            RadioTelegraphModePayload::handle);
        // 收发报机命名
        registrar.playToServer(RadioTelegraphNamePayload.TYPE, RadioTelegraphNamePayload.STREAM_CODEC,
            RadioTelegraphNamePayload::handle);
        // 收发报机配对
        registrar.playToServer(RadioTelegraphPairPayload.TYPE, RadioTelegraphPairPayload.STREAM_CODEC,
            RadioTelegraphPairPayload::handle);
        // 收发报机：请求可用接收器列表（客户端 → 服务器）
        registrar.playToServer(RadioTelegraphRequestReceiversPayload.TYPE, RadioTelegraphRequestReceiversPayload.STREAM_CODEC,
            RadioTelegraphRequestReceiversPayload::handle);
        // 收发报机：可用接收器列表响应（服务器 → 客户端）
        registrar.playToClient(RadioTelegraphReceiversPayload.TYPE, RadioTelegraphReceiversPayload.STREAM_CODEC,
            RadioTelegraphReceiversPayload::handle);
        // 收发报机：节日音效成就上报（客户端 → 服务器）
        registrar.playToServer(RadioTelegraphFestivalPayload.TYPE, RadioTelegraphFestivalPayload.STREAM_CODEC,
            RadioTelegraphFestivalPayload::handle);
    }

    public static ResourceLocation rl(String path) {
        return ResourceLocation.fromNamespaceAndPath(CreateMassenergy.MODID, path);
    }
}
