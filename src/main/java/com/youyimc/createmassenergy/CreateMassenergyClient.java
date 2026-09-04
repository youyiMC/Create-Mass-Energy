package com.youyimc.createmassenergy;

import com.youyimc.createmassenergy.client.gui.AnnihilationFurnaceScreen;
import com.youyimc.createmassenergy.client.gui.DataTerminalScreen;
import com.youyimc.createmassenergy.client.gui.RadioTelegraphScreen;
import com.youyimc.createmassenergy.client.sound.DataTerminalSoundHandler;
import com.youyimc.createmassenergy.client.sound.RadioTelegraphSoundHandler;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;

// This class will not load on dedicated servers. Accessing client side code from here is safe.
@Mod(value = CreateMassenergy.MODID, dist = Dist.CLIENT)
public class CreateMassenergyClient {
    public CreateMassenergyClient(IEventBus modEventBus, ModContainer container) {
        // Allows NeoForge to create a config screen for this mod's configs.
        // The config screen is accessed by going to the Mods screen > clicking on your mod > clicking on config.
        // Do not forget to add translations for your config options to the en_us.json file.
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);

        // 注册客户端屏幕
        modEventBus.addListener(CreateMassenergyClient::registerScreens);
        modEventBus.addListener(CreateMassenergyClient::onClientSetup);

        // 注册数据化终端循环音效驱动（游戏总线事件）
        NeoForge.EVENT_BUS.addListener(CreateMassenergyClient::onRenderLevelStage);
    }

    private static void onClientSetup(FMLClientSetupEvent event) {
        CreateMassenergy.LOGGER.info("HELLO FROM CLIENT SETUP");
    }

    private static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenuTypes.ANNIHILATION_FURNACE.get(), AnnihilationFurnaceScreen::new);
        event.register(ModMenuTypes.DATA_TERMINAL.get(), DataTerminalScreen::new);
        event.register(ModMenuTypes.RADIO_TELEGRAPH.get(), RadioTelegraphScreen::new);
    }

    /** 每帧驱动数据化终端工作循环音效 与 收发报机音效 */
    private static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_ENTITIES) {
            DataTerminalSoundHandler.tick();
            RadioTelegraphSoundHandler.tick();
        }
    }
}
