package sylenthuntress.thermia.config;

import net.minecraft.entity.EntityType;
import sylenthuntress.thermia.Thermia;

import java.util.List;

public abstract class ConfigHelper {
    @SuppressWarnings("deprecation")
    private static boolean isInStringList(EntityType<?> entityType, List<String> list, boolean invertList) {
        return (entityType.getRegistryEntry().streamTags().map(
                key -> "#" + key.id().toString()
        ).anyMatch(list::contains) || list.contains(EntityType.getId(entityType).toString()))
                != invertList;
    }

    public static boolean isInList$climateUnaffected(EntityType<?> entityType) {
        return isInStringList(
                entityType,
                Thermia.CONFIG.entityTemperature.entityTags.UNAFFECTED_LIST(),
                Thermia.CONFIG.entityTemperature.entityTags.UNAFFECTED_LIST_INVERT()
        );
    }

    public static boolean isInList$hasFur(EntityType<?> entityType) {
        return isInStringList(
                entityType,
                Thermia.CONFIG.entityTemperature.entityTags.HAS_FUR_LIST(),
                Thermia.CONFIG.entityTemperature.entityTags.HAS_FUR_LIST_INVERT()
        );
    }

    public static boolean isInList$hasWool(EntityType<?> entityType) {
        return isInStringList(
                entityType,
                Thermia.CONFIG.entityTemperature.entityTags.HAS_WOOL_LIST(),
                Thermia.CONFIG.entityTemperature.entityTags.HAS_WOOL_LIST_INVERT()
        );
    }

    public static boolean isInList$Undead(EntityType<?> entityType) {
        return isInStringList(
                entityType,
                Thermia.CONFIG.entityTemperature.entityTags.UNDEAD_LIST(),
                Thermia.CONFIG.entityTemperature.entityTags.UNDEAD_LIST_INVERT()
        );
    }
}
