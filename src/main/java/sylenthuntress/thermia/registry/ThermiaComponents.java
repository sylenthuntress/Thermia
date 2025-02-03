package sylenthuntress.thermia.registry;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.component.ComponentType;
import net.minecraft.item.consume.ConsumeEffect;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import sylenthuntress.thermia.Thermia;
import sylenthuntress.thermia.registry.data_components.TemperatureComponent;

public class ThermiaComponents {
    public static final ComponentType<TemperatureComponent> TEMPERATURE_COMPONENT = Registry.register(
            Registries.DATA_COMPONENT_TYPE,
            Thermia.modIdentifier("temperature"),
            ComponentType.<TemperatureComponent>builder().codec(TemperatureComponent.CODEC).build()
    );

    public static void registerAll() {

    }
}
