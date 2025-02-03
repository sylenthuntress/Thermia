package sylenthuntress.thermia.registry.data_components;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.component.type.Consumable;
import net.minecraft.component.type.ConsumableComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.consume.ConsumeEffect;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.world.World;
import sylenthuntress.thermia.access.temperature.LivingEntityAccess;
import sylenthuntress.thermia.registry.ThermiaComponents;
import sylenthuntress.thermia.temperature.TemperatureManager;

public record TemperatureComponent(double temperature, double minTemperature, double maxTemperature, boolean applyModifiers) implements Consumable {
    public static final Codec<TemperatureComponent> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                            Codec.DOUBLE.fieldOf("temperature").forGetter(TemperatureComponent::temperature),
                            Codec.DOUBLE.optionalFieldOf("min_temperature", 0.0).forGetter(TemperatureComponent::minTemperature),
                            Codec.DOUBLE.optionalFieldOf("max_temperature", 0.0).forGetter(TemperatureComponent::maxTemperature),
                            Codec.BOOL.optionalFieldOf("apply_modifiers", true).forGetter(TemperatureComponent::applyModifiers)
                    )
                    .apply(instance, TemperatureComponent::new)
    );

    @Override
    public void onConsume(World world, LivingEntity user, ItemStack stack, ConsumableComponent consumable) {
        double temperature = temperature() + world.getRandom().nextBetween(
                (int) Math.round(minTemperature * 1000),
                (int) Math.round(maxTemperature * 1000)) * 0.001;
        if (temperature != 0) {
            TemperatureManager temperatureManager = ((LivingEntityAccess)user).thermia$getTemperatureManager();
            temperatureManager.modifyTemperature(temperature);
        }
    }
}
