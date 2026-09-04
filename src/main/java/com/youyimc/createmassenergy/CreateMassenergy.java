package com.youyimc.createmassenergy;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;
import com.youyimc.createmassenergy.advancement.ModAdvancementTriggers;
import com.youyimc.createmassenergy.block.entity.AnnihilationFurnaceBlockEntity;
import com.youyimc.createmassenergy.block.entity.DataTerminalBlockEntity;
import com.youyimc.createmassenergy.block.entity.RadioTelegraphBlockEntity;
import com.youyimc.createmassenergy.compat.AeronauticsCompat;
import com.youyimc.createmassenergy.data.ModDataGenerator;
import com.youyimc.createmassenergy.network.ModNetworking;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(CreateMassenergy.MODID)
public class CreateMassenergy {
    // Define mod id in a common place for everything to reference
    public static final String MODID = "createmassenergy";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();

    /** 快捷创建本模组的 ResourceLocation */
    public static net.minecraft.resources.ResourceLocation rl(String path) {
        return net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(MODID, path);
    }

    // The constructor for the mod class is the first code that is run when your mod is loaded.
    // FML will recognize some parameter types like IEventBus or ModContainer and pass them in automatically.
    public CreateMassenergy(IEventBus modEventBus, ModContainer modContainer) {
        // Register Deferred Registers
        ModBlocks.BLOCKS.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModBlockEntities.BLOCK_ENTITIES.register(modEventBus);
        ModMenuTypes.MENUS.register(modEventBus);
        ModCreativeTabs.TABS.register(modEventBus);
        ModSounds.SOUNDS.register(modEventBus);

        // Register network payloads
        ModNetworking.register(modEventBus);

        // Register data generator
        ModDataGenerator.register(modEventBus);

        // Register custom advancement triggers
        modEventBus.addListener(ModAdvancementTriggers::register);

        // Register capability providers
        modEventBus.addListener(this::registerCapabilities);

        // Register the mod's ModConfigSpec so that FML can create and load the config file for us
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);

        // 航空学物理结构软依赖联动
        // 注意：DeferredHolder 只有在方块注册完成（RegisterEvent）后才能安全 get()，
        // 因此这里只登记 DeferredBlock 引用，在 RegisterEvent 里再解析并初始化。
        modEventBus.addListener(this::onRegisterBlocks);
    }

    /**
     * 方块注册完成后初始化航空学联动。
     * <p>
     * 此时 {@link ModBlocks} 的 DeferredBlock 已绑定到 registry，可以安全调用 get()。
     * 若 Sable 未加载，此方法内部会安全跳过。
     */
    private void onRegisterBlocks(net.neoforged.neoforge.registries.RegisterEvent event) {
        if (!event.getRegistryKey().equals(net.minecraft.core.registries.Registries.BLOCK)) {
            return;
        }
        AeronauticsCompat.init(java.util.List.of(
                ModBlocks.ANNIHILATION_FURNACE,
                ModBlocks.DATA_TERMINAL,
                ModBlocks.RADIO_TELEGRAPH));
    }

    private void registerCapabilities(RegisterCapabilitiesEvent event) {
        AnnihilationFurnaceBlockEntity.registerCapabilities(event);
        DataTerminalBlockEntity.registerCapabilities(event);
        RadioTelegraphBlockEntity.registerCapabilities(event);
    }
}
