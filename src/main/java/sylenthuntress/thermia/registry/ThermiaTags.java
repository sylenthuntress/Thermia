package sylenthuntress.thermia.registry;

import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import sylenthuntress.thermia.Thermia;

public class ThermiaTags {
    public static final TagKey<EntityType<?>> COLD_MOBS = TagKey.of(
            RegistryKeys.ENTITY_TYPE,
            Thermia.modIdentifier("climate/cold_mobs")
    );
    public static final TagKey<EntityType<?>> HOT_MOBS = TagKey.of(
            RegistryKeys.ENTITY_TYPE,
            Thermia.modIdentifier("climate/hot_mobs")
    );
    public static final TagKey<EntityType<?>> NETHER_MOBS = TagKey.of(
            RegistryKeys.ENTITY_TYPE,
            Thermia.modIdentifier("climate/nether_mobs")
    );
    public static final TagKey<EntityType<?>> UNDEAD_MOBS = TagKey.of(
            RegistryKeys.ENTITY_TYPE,
            Thermia.modIdentifier("climate/undead_mobs")
    );
    public static final TagKey<EntityType<?>> TEMPERATURE_IMMUNE = TagKey.of(
            RegistryKeys.ENTITY_TYPE,
            Thermia.modIdentifier("climate/temperature_immune")
    );
    public static final TagKey<EntityType<?>> CLIMATE_AFFECTED = TagKey.of(
            RegistryKeys.ENTITY_TYPE,
            Thermia.modIdentifier("climate/affected")
    );
    public static final TagKey<Item> COLD_FOODS = TagKey.of(
            RegistryKeys.ITEM,
            Thermia.modIdentifier("consumable_temperature/cold_foods")
    );
    public static final TagKey<Item> HOT_FOODS = TagKey.of(
            RegistryKeys.ITEM,
            Thermia.modIdentifier("consumable_temperature/hot_foods")
    );
    public static final TagKey<Item> REFRESHING_FOODS = TagKey.of(
            RegistryKeys.ITEM,
            Thermia.modIdentifier("consumable_temperature/refreshing_foods")
    );
    public static final TagKey<Item> WARM_FOODS = TagKey.of(
            RegistryKeys.ITEM,
            Thermia.modIdentifier("consumable_temperature/warm_foods")
    );
}
