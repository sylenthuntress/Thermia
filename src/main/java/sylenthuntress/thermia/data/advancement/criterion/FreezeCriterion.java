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

public class FreezeCriterion extends AbstractCriterion<FreezeCriterion.Conditions> {
    @Override
    public Codec<Conditions> getConditionsCodec() {
        return Conditions.CODEC;
    }

    public void trigger(ServerPlayerEntity player, int amplifier) {
        this.trigger(player, conditions -> conditions.matches(amplifier));
    }

    public record Conditions(Optional<LootContextPredicate> player,
                             NumberRange.IntRange amplifier) implements AbstractCriterion.Conditions {
        public static final Codec<FreezeCriterion.Conditions> CODEC = RecordCodecBuilder.create(
                instance -> instance.group(
                                EntityPredicate.LOOT_CONTEXT_PREDICATE_CODEC.optionalFieldOf("player").forGetter(FreezeCriterion.Conditions::player),
                                NumberRange.IntRange.CODEC.optionalFieldOf("amplifier", NumberRange.IntRange.ANY).forGetter(FreezeCriterion.Conditions::amplifier)
                        )
                        .apply(instance, FreezeCriterion.Conditions::new)
        );

        public static AdvancementCriterion<FreezeCriterion.Conditions> create() {
            return create(NumberRange.IntRange.ANY);
        }

        public static AdvancementCriterion<FreezeCriterion.Conditions> create(NumberRange.IntRange amplifier) {
            return ThermiaCriteria.PLAYER_FROZEN.create(
                    new FreezeCriterion.Conditions(
                            Optional.empty(),
                            amplifier
                    )
            );
        }

        public boolean matches(int amplifier) {
            return amplifier().test(amplifier);
        }
    }
}
