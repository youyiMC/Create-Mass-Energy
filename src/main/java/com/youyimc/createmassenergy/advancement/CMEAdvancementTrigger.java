package com.youyimc.createmassenergy.advancement;

import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;

/**
 * 自定义成就触发器（用于"首次触发获得"的简单触发器）
 */
public class CMEAdvancementTrigger extends SimpleCriterionTrigger<CMEAdvancementTrigger.TriggerInstance> {

    public static final CMEAdvancementTrigger INSTANCE = new CMEAdvancementTrigger();

    @Override
    public Codec<TriggerInstance> codec() {
        return TriggerInstance.CODEC;
    }

    /** 在服务器端触发成就 */
    public void trigger(ServerPlayer player) {
        this.trigger(player, instance -> true);
    }

    public static record TriggerInstance(Optional<ContextAwarePredicate> player) implements SimpleCriterionTrigger.SimpleInstance {

        public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ContextAwarePredicate.CODEC.optionalFieldOf("player").forGetter(TriggerInstance::player)
        ).apply(instance, TriggerInstance::new));

        public static Criterion<TriggerInstance> criterion() {
            return INSTANCE.createCriterion(new TriggerInstance(Optional.empty()));
        }
    }
}
