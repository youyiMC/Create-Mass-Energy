package com.youyimc.createmassenergy.advancement;

import com.youyimc.createmassenergy.CreateMassenergy;

import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.registries.RegisterEvent;

/**
 * 自定义成就触发器注册（NeoForge 需要在 RegisterEvent 中注册触发器）
 */
public class ModAdvancementTriggers {

    /** 触发器的注册 ID */
    public static final ResourceLocation TRIGGER_ID =
        ResourceLocation.fromNamespaceAndPath(CreateMassenergy.MODID, "cme_trigger");

    public static void register(RegisterEvent event) {
        if (event.getRegistryKey().equals(net.minecraft.core.registries.Registries.TRIGGER_TYPE)) {
            event.register(net.minecraft.core.registries.Registries.TRIGGER_TYPE, TRIGGER_ID, () -> CMEAdvancementTrigger.INSTANCE);
        }
    }
}
