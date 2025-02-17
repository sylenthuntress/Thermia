package sylenthuntress.thermia.data.advancement.criterion;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancement.AdvancementCriterion;
import net.minecraft.advancement.criterion.AbstractCriterion;
import net.minecraft.predicate.NumberRange;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.predicate.entity.LootContextPredicate;
import net.minecraft.server.network.ServerPlayerEntity;
import sylenthuntress.thermia.registry.ThermiaCriteria;

import java.util.Optional;

public class TemperatureChangedCriterion extends AbstractCriterion<TemperatureChangedCriterion.Conditions> {
    @Override
    public Codec<Conditions> getConditionsCodec() {
        return Conditions.CODEC;
    }

    public void trigger(ServerPlayerEntity player, double temperature, boolean modified) {
        this.trigger(player, conditions -> conditions.matches(temperature, modified));
    }

    public record Conditions(Optional<LootContextPredicate> player, NumberRange.DoubleRange temperature,
                             boolean modified) implements AbstractCriterion.Conditions {
        public static final Codec<TemperatureChangedCriterion.Conditions> CODEC = RecordCodecBuilder.create(
                instance -> instance.group(
                                EntityPredicate.LOOT_CONTEXT_PREDICATE_CODEC.optionalFieldOf("player").forGetter(TemperatureChangedCriterion.Conditions::player),
                                NumberRange.DoubleRange.CODEC.optionalFieldOf("temperature", NumberRange.DoubleRange.ANY).forGetter(TemperatureChangedCriterion.Conditions::temperature),
                                Codec.BOOL.optionalFieldOf("modified", true).forGetter(TemperatureChangedCriterion.Conditions::modified)
                        )
                        .apply(instance, TemperatureChangedCriterion.Conditions::new)
        );

        public static AdvancementCriterion<TemperatureChangedCriterion.Conditions> create(NumberRange.DoubleRange temperature, boolean modified) {
            return ThermiaCriteria.TEMPERATURE_CHANGED.create(
                    new TemperatureChangedCriterion.Conditions(
                            Optional.empty(),
                            temperature,
                            modified
                    )
            );
        }

        public boolean matches(double temperature, boolean modified) {
            return temperature().test(temperature) && modified == modified();
        }
    }
}
