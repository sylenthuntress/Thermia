package sylenthuntress.thermia.registry;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.EntityTypeTags;
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
}
