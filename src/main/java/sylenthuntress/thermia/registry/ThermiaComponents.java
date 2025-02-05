package sylenthuntress.thermia.registry;

import net.minecraft.component.ComponentType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import sylenthuntress.thermia.Thermia;
import sylenthuntress.thermia.registry.data_components.ConsumableTemperatureComponent;

public class ThermiaComponents {
    public static final ComponentType<ConsumableTemperatureComponent> CONSUMABLE_TEMPERATURE_COMPONENT = Registry.register(
            Registries.DATA_COMPONENT_TYPE,
            Thermia.modIdentifier("consumable_temperature"),
            ComponentType.<ConsumableTemperatureComponent>builder().codec(ConsumableTemperatureComponent.CODEC).build()
    );

    public static void registerAll() {

    }
}
