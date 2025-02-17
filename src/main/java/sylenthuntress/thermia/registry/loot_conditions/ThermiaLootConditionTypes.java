package sylenthuntress.thermia.registry.loot_conditions;

import com.mojang.serialization.MapCodec;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.condition.LootConditionType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import sylenthuntress.thermia.Thermia;
import sylenthuntress.thermia.data.predicate.EntityTemperatureLootCondition;

public class ThermiaLootConditionTypes {
    public static final LootConditionType ENTITY_TEMPERATURE = register(Thermia.modIdentifier("entity_temperature"), EntityTemperatureLootCondition.CODEC);

    private static LootConditionType register(Identifier id, MapCodec<? extends LootCondition> codec) {
        return Registry.register(Registries.LOOT_CONDITION_TYPE, id, new LootConditionType(codec));
    }

    public static void registerAll() {

    }
}
