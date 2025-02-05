package sylenthuntress.thermia.registry;

import net.minecraft.entity.attribute.ClampedEntityAttribute;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import sylenthuntress.thermia.Thermia;

public class ThermiaAttributes {
    public static final RegistryEntry.Reference<EntityAttribute> BODY_TEMPERATURE = Registry.registerReference(
            Registries.ATTRIBUTE,
            Thermia.modIdentifier("body_temperature"),
            new ClampedEntityAttribute(
                    "attribute.name.generic.body_temperature",
                    97,
                    -100,
                    200
            )
    );
    public static final RegistryEntry.Reference<EntityAttribute> COLD_MODIFIER = Registry.registerReference(
            Registries.ATTRIBUTE,
            Thermia.modIdentifier("cold_modifier"),
            new ClampedEntityAttribute(
                    "attribute.name.generic.cold_modifier",
                    1,
                    0,
                    255
            )
    );
    public static final RegistryEntry.Reference<EntityAttribute> HEAT_MODIFIER = Registry.registerReference(
            Registries.ATTRIBUTE,
            Thermia.modIdentifier("heat_modifier"),
            new ClampedEntityAttribute(
                    "attribute.name.generic.heat_modifier",
                    1,
                    0,
                    255
            )
    );
    public static final RegistryEntry.Reference<EntityAttribute> HEAT_OFFSET_THRESHOLD = Registry.registerReference(
            Registries.ATTRIBUTE,
            Thermia.modIdentifier("heat_offset_threshold"),
            new ClampedEntityAttribute(
                    "attribute.name.generic.heat_offset_threshold",
                    3,
                    -100,
                    200
            )
    );
    public static final RegistryEntry.Reference<EntityAttribute> COLD_OFFSET_THRESHOLD = Registry.registerReference(
            Registries.ATTRIBUTE,
            Thermia.modIdentifier("cold_offset_threshold"),
            new ClampedEntityAttribute(
                    "attribute.name.generic.cold_offset_threshold",
                    -2,
                    -100,
                    200
            )
    );
    public static void registerAll() {

    }
}