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

public class OverheatCriterion extends AbstractCriterion<OverheatCriterion.Conditions> {
    @Override
    public Codec<Conditions> getConditionsCodec() {
        return Conditions.CODEC;
    }

    public void trigger(ServerPlayerEntity player, int amplifier) {
        this.trigger(player, conditions -> conditions.matches(amplifier));
    }

    public record Conditions(Optional<LootContextPredicate> player,
                             NumberRange.IntRange amplifier) implements AbstractCriterion.Conditions {
        public static final Codec<OverheatCriterion.Conditions> CODEC = RecordCodecBuilder.create(
                instance -> instance.group(
                                EntityPredicate.LOOT_CONTEXT_PREDICATE_CODEC.optionalFieldOf("player").forGetter(OverheatCriterion.Conditions::player),
                                NumberRange.IntRange.CODEC.optionalFieldOf("amplifier", NumberRange.IntRange.ANY).forGetter(OverheatCriterion.Conditions::amplifier)
                        )
                        .apply(instance, OverheatCriterion.Conditions::new)
        );

        public static AdvancementCriterion<OverheatCriterion.Conditions> create() {
            return create(NumberRange.IntRange.ANY);
        }

        public static AdvancementCriterion<OverheatCriterion.Conditions> create(NumberRange.IntRange amplifier) {
            return ThermiaCriteria.PLAYER_OVERHEATING.create(
                    new OverheatCriterion.Conditions(
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
