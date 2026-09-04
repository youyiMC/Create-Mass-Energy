package com.youyimc.createmassenergy.data;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import com.youyimc.createmassenergy.ModItems;
import com.youyimc.createmassenergy.advancement.CMEAdvancementTrigger;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.advancements.AdvancementProvider;
import net.minecraft.data.advancements.AdvancementSubProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * 成就生成器
 * <p>
 * 所有成就均为首次触发获得：
 * <ul>
 *   <li>Pentium 4 HT：为任意机器安装任意多线程升级模块</li>
 *   <li>胶水4核：获得4线程升级模块</li>
 *   <li>绿皮科技：获得32线程升级模块</li>
 *   <li>多路CPU：在同一个机器的插槽里装1个以上线程升级模块</li>
 *   <li>华擎妖板：在同一个机器的插槽里装不同等级的线程升级模块</li>
 *   <li>IBM 350：为机器安装任意存储升级模块</li>
 *   <li>马可尼时代：16格内收发器接收或发送物品数据一次</li>
 *   <li>线程阻塞：16格内任意收发器全线程满载运行</li>
 * </ul>
 */
public class ModAdvancementProvider extends AdvancementProvider {

    public ModAdvancementProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries,
        net.neoforged.neoforge.common.data.ExistingFileHelper existingFileHelper) {
        super(output, registries, java.util.List.of(new CMEAdvancements()));
    }

    public static class CMEAdvancements implements AdvancementSubProvider {

        @Override
        public void generate(HolderLookup.Provider registries, Consumer<AdvancementHolder> consumer) {
            AdvancementHolder root = Advancement.Builder.advancement()
                .display(ModItems.DUAL_CORE.get(),
                    Component.translatable("advancement.createmassenergy.root"),
                    Component.translatable("advancement.createmassenergy.root.desc"),
                    ResourceLocation.fromNamespaceAndPath("minecraft", "textures/block/andesite.png"),
                    AdvancementType.TASK, true, true, false)
                .addCriterion("has_dual_core", InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.DUAL_CORE.get()))
                .save(consumer, "createmassenergy:root");

            // Pentium 4 HT：为任意机器安装任意多线程升级模块
            AdvancementHolder pentium4 = Advancement.Builder.advancement()
                .parent(root)
                .display(ModItems.DUAL_CORE.get(),
                    Component.translatable("advancement.createmassenergy.pentium4_ht"),
                    Component.translatable("advancement.createmassenergy.pentium4_ht.desc"),
                    null, AdvancementType.TASK, true, true, false)
                .addCriterion("trigger", CMEAdvancementTrigger.TriggerInstance.criterion())
                .save(consumer, "createmassenergy:pentium4_ht");

            // 胶水4核：获得4线程升级模块
            Advancement.Builder.advancement()
                .parent(pentium4)
                .display(ModItems.QUAD_CORE.get(),
                    Component.translatable("advancement.createmassenergy.glue_quad_core"),
                    Component.translatable("advancement.createmassenergy.glue_quad_core.desc"),
                    null, AdvancementType.TASK, true, true, false)
                .addCriterion("has_quad_core", InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.QUAD_CORE.get()))
                .save(consumer, "createmassenergy:glue_quad_core");

            // 绿皮科技：获得32线程升级模块
            Advancement.Builder.advancement()
                .parent(root)
                .display(ModItems.DOTRIDECIMAL_CORE.get(),
                    Component.translatable("advancement.createmassenergy.green_skin_tech"),
                    Component.translatable("advancement.createmassenergy.green_skin_tech.desc"),
                    null, AdvancementType.CHALLENGE, true, true, false)
                .addCriterion("has_32_core", InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.DOTRIDECIMAL_CORE.get()))
                .save(consumer, "createmassenergy:green_skin_tech");

            // 多路CPU：在同一个机器的插槽里装1个以上线程升级模块
            Advancement.Builder.advancement()
                .parent(root)
                .display(ModItems.OCTO_CORE.get(),
                    Component.translatable("advancement.createmassenergy.multi_cpu"),
                    Component.translatable("advancement.createmassenergy.multi_cpu.desc"),
                    null, AdvancementType.TASK, true, true, false)
                .addCriterion("trigger", CMEAdvancementTrigger.TriggerInstance.criterion())
                .save(consumer, "createmassenergy:multi_cpu");

            // 华擎妖板：在同一个机器的插槽里装不同等级的线程升级模块
            Advancement.Builder.advancement()
                .parent(root)
                .display(ModItems.HEXADECIMAL_CORE.get(),
                    Component.translatable("advancement.createmassenergy.asrock"),
                    Component.translatable("advancement.createmassenergy.asrock.desc"),
                    null, AdvancementType.CHALLENGE, true, true, false)
                .addCriterion("trigger", CMEAdvancementTrigger.TriggerInstance.criterion())
                .save(consumer, "createmassenergy:asrock");

            // IBM 350：为机器安装任意存储升级模块
            Advancement.Builder.advancement()
                .parent(root)
                .display(ModItems.HARD_DISK_16GB.get(),
                    Component.translatable("advancement.createmassenergy.ibm350"),
                    Component.translatable("advancement.createmassenergy.ibm350.desc"),
                    null, AdvancementType.TASK, true, true, false)
                .addCriterion("trigger", CMEAdvancementTrigger.TriggerInstance.criterion())
                .save(consumer, "createmassenergy:ibm350");

            // 马可尼时代：16格内收发器接收或发送物品数据一次
            Advancement.Builder.advancement()
                .parent(root)
                .display(ModItems.RADIO_TELEGRAPH_ITEM.get(),
                    Component.translatable("advancement.createmassenergy.marconi_era"),
                    Component.translatable("advancement.createmassenergy.marconi_era.desc"),
                    null, AdvancementType.TASK, true, true, false)
                .addCriterion("trigger", CMEAdvancementTrigger.TriggerInstance.criterion())
                .save(consumer, "createmassenergy:marconi_era");

            // 线程阻塞：16格内任意收发器全线程满载运行
            Advancement.Builder.advancement()
                .parent(root)
                .display(ModItems.DUAL_CORE.get(),
                    Component.translatable("advancement.createmassenergy.thread_block"),
                    Component.translatable("advancement.createmassenergy.thread_block.desc"),
                    null, AdvancementType.CHALLENGE, true, true, false)
                .addCriterion("trigger", CMEAdvancementTrigger.TriggerInstance.criterion())
                .save(consumer, "createmassenergy:thread_block");
        }
    }
}
