package com.youyimc.createmassenergy.item;

import java.util.List;

import com.youyimc.createmassenergy.upgrade.UpgradeDefinition;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

/**
 * 升级模块物品基类
 */
public class UpgradeItem extends Item {

    private final UpgradeDefinition definition;

    public UpgradeItem(Properties properties, UpgradeDefinition definition) {
        super(properties);
        this.definition = definition;
    }

    public UpgradeDefinition getDefinition() {
        return definition;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents,
        TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
        tooltipComponents.add(Component.translatable(definition.descriptionKey()).withStyle(ChatFormatting.GRAY));
        tooltipComponents.add(Component.translatable("tooltip.createmassenergy.upgrade.type")
            .append(Component.translatable("tooltip.createmassenergy.upgrade.type." + definition.type().getSerializedName()))
            .withStyle(ChatFormatting.DARK_GRAY));
    }
}
