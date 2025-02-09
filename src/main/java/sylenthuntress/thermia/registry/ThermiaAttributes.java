package sylenthuntress.thermia.registry;

import net.minecraft.entity.attribute.ClampedEntityAttribute;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import sylenthuntress.thermia.Thermia;

public class ThermiaAttributes {
    public static final RegistryEntry.Reference<EntityAttribute> BASE_TEMPERATURE = Registry.registerReference(
            Registries.ATTRIBUTE,
            Thermia.modIdentifier("base_temperature"),
            new ClampedEntityAttribute(
                    "attribute.name.generic.base_temperature",
                    97,
                    -100,
                    200
            ).setTracked(true).setCategory(EntityAttribute.Category.POSITIVE)
    );
    public static final RegistryEntry.Reference<EntityAttribute> HEAT_OFFSET_THRESHOLD = Registry.registerReference(
            Registries.ATTRIBUTE,
            Thermia.modIdentifier("heat_offset_threshold"),
            new ClampedEntityAttribute(
                    "attribute.name.generic.heat_offset_threshold",
                    3,
                    -255,
                    255
            ).setTracked(true).setCategory(EntityAttribute.Category.POSITIVE)
    );
    public static final RegistryEntry.Reference<EntityAttribute> COLD_OFFSET_THRESHOLD = Registry.registerReference(
            Registries.ATTRIBUTE,
            Thermia.modIdentifier("cold_offset_threshold"),
            new ClampedEntityAttribute(
                    "attribute.name.generic.cold_offset_threshold",
                    2,
                    -255,
                    255
            ).setTracked(true).setCategory(EntityAttribute.Category.POSITIVE)
    );

    public static void registerAll() {

    }
}