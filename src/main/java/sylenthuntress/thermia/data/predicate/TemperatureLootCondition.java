package sylenthuntress.thermia.data.predicate;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.condition.LootConditionType;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.util.context.ContextParameter;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import sylenthuntress.thermia.registry.loot_conditions.ThermiaLootConditionTypes;

import java.util.Optional;
import java.util.Set;

public record TemperatureLootCondition(Optional<EntityTemperaturePredicate> entityPredicate,
                                       Optional<LocationTemperaturePredicate> locationPredicate,
                                       Optional<LootContext.EntityTarget> entity,
                                       BlockPos offset) implements LootCondition {
    private static final MapCodec<BlockPos> OFFSET_CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(
                            Codec.INT.optionalFieldOf("offsetX", 0).forGetter(Vec3i::getX),
                            Codec.INT.optionalFieldOf("offsetY", 0).forGetter(Vec3i::getY),
                            Codec.INT.optionalFieldOf("offsetZ", 0).forGetter(Vec3i::getZ)
                    )
                    .apply(instance, BlockPos::new)
    );

    public static final MapCodec<TemperatureLootCondition> CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(
                            EntityTemperaturePredicate.CODEC.optionalFieldOf("entity_predicate").forGetter(TemperatureLootCondition::entityPredicate),
                            LocationTemperaturePredicate.CODEC.optionalFieldOf("location_predicate").forGetter(TemperatureLootCondition::locationPredicate),
                            LootContext.EntityTarget.CODEC.optionalFieldOf("entity").forGetter(TemperatureLootCondition::entity),
                            OFFSET_CODEC.forGetter(TemperatureLootCondition::offset)
                    )
                    .apply(instance, TemperatureLootCondition::new)
    );

    public static LootCondition.Builder create(LootContext.EntityTarget entity) {
        return builder(entity, EntityTemperaturePredicate.Builder.create(), LocationTemperaturePredicate.Builder.create());
    }

    public static LootCondition.Builder builder(LootContext.EntityTarget entity, EntityTemperaturePredicate.Builder entityPredicateBuilder, LocationTemperaturePredicate.Builder locationPredicateBuilder) {
        return () -> new TemperatureLootCondition(
                Optional.of(entityPredicateBuilder.build()),
                Optional.of(locationPredicateBuilder.build()),
                Optional.of(entity),
                BlockPos.ORIGIN
        );
    }

    @Override
    public LootConditionType getType() {
        return ThermiaLootConditionTypes.TEMPERATURE;
    }

    @Override
    public Set<ContextParameter<?>> getAllowedParameters() {
        return this.entity.map(entityTarget -> Set.of(
                LootContextParameters.ORIGIN,
                entityTarget.getParameter()
        )).orElseGet(() -> Set.of(LootContextParameters.ORIGIN));
    }

    @SuppressWarnings({"OptionalGetWithoutIsPresent", "DataFlowIssue"})
    public boolean test(LootContext lootContext) {
        Vec3d origin = lootContext.get(LootContextParameters.ORIGIN);
        return (this.entityPredicate.isEmpty() || this.entityPredicate.get().test(lootContext.get(this.entity.get().getParameter())))
                && (this.locationPredicate.isEmpty() || this.locationPredicate.get().test(lootContext.getWorld(), BlockPos.ofFloored(origin)));
    }
}
