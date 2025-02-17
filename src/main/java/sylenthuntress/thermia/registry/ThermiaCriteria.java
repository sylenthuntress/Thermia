package sylenthuntress.thermia.registry;

import net.minecraft.advancement.criterion.Criterion;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import sylenthuntress.thermia.Thermia;
import sylenthuntress.thermia.data.advancement.criterion.FreezeCriterion;
import sylenthuntress.thermia.data.advancement.criterion.OverheatCriterion;
import sylenthuntress.thermia.data.advancement.criterion.TemperatureChangedCriterion;

public final class ThermiaCriteria {
    public static FreezeCriterion PLAYER_FROZEN = register(Thermia.modIdentifier("player_frozen"), new FreezeCriterion());
    public static OverheatCriterion PLAYER_OVERHEATING = register(Thermia.modIdentifier("player_overheating"), new OverheatCriterion());
    public static TemperatureChangedCriterion TEMPERATURE_CHANGED = register(Thermia.modIdentifier("temperature_changed"), new TemperatureChangedCriterion());

    public static <T extends Criterion<?>> T register(Identifier id, T criterion) {
        return Registry.register(Registries.CRITERION, id, criterion);
    }

    public static void registerAll() {

    }
}
