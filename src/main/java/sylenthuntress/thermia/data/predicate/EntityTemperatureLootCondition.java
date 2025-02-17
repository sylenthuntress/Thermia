package sylenthuntress.thermia.data.predicate;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.condition.LootConditionType;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.util.context.ContextParameter;
import sylenthuntress.thermia.registry.loot_conditions.ThermiaLootConditionTypes;

import java.util.Optional;
import java.util.Set;

public record EntityTemperatureLootCondition(Optional<EntityTemperaturePredicate> predicate,
                                             LootContext.EntityTarget entity) implements LootCondition {
    public static final MapCodec<EntityTemperatureLootCondition> CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(
                            EntityTemperaturePredicate.CODEC.optionalFieldOf("predicate").forGetter(EntityTemperatureLootCondition::predicate),
                            LootContext.EntityTarget.CODEC.fieldOf("entity").forGetter(EntityTemperatureLootCondition::entity)
                    )
                    .apply(instance, EntityTemperatureLootCondition::new)
    );

    public static LootCondition.Builder create(LootContext.EntityTarget entity) {
        return builder(entity, EntityTemperaturePredicate.Builder.create());
    }

    public static LootCondition.Builder builder(LootContext.EntityTarget entity, EntityTemperaturePredicate.Builder predicateBuilder) {
        return () -> new EntityTemperatureLootCondition(Optional.of(predicateBuilder.build()), entity);
    }

    @Override
    public LootConditionType getType() {
        return ThermiaLootConditionTypes.ENTITY_TEMPERATURE;
    }

    @Override
    public Set<ContextParameter<?>> getAllowedParameters() {
        return Set.of(LootContextParameters.ORIGIN, this.entity.getParameter());
    }

    public boolean test(LootContext lootContext) {
        return this.predicate.isEmpty() || this.predicate.get().test(lootContext.get(this.entity.getParameter()));
    }
}
