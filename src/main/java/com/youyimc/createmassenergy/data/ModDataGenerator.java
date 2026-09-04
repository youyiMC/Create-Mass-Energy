package com.youyimc.createmassenergy.data;

import java.util.concurrent.CompletableFuture;

import com.youyimc.createmassenergy.CreateMassenergy;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;

/**
 * 数据生成器入口
 */
public final class ModDataGenerator {
    private ModDataGenerator() {}

    /** 在 mod 构造时调用注册 */
    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(ModDataGenerator::gatherData);
    }

    private static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput packOutput = generator.getPackOutput();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();

        generator.addProvider(event.includeServer(), new ModRecipeProvider(packOutput, lookupProvider));
        generator.addProvider(event.includeServer(), new ModAdvancementProvider(packOutput, lookupProvider, existingFileHelper));
    }
}
