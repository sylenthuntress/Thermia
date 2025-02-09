package sylenthuntress.thermia.registry.data_components;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.component.type.Consumable;
import net.minecraft.component.type.ConsumableComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import sylenthuntress.thermia.access.temperature.LivingEntityAccess;
import sylenthuntress.thermia.temperature.TemperatureManager;

import java.util.Random;

public record ConsumableTemperatureComponent(double temperature, double minTemperature,
                                             double maxTemperature) implements Consumable {
    public static final Codec<ConsumableTemperatureComponent> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                            Codec.DOUBLE.fieldOf("temperature").forGetter(ConsumableTemperatureComponent::temperature),
                            Codec.DOUBLE.optionalFieldOf("min_temperature", 0.0).forGetter(ConsumableTemperatureComponent::minTemperature),
                            Codec.DOUBLE.optionalFieldOf("max_temperature", 0.0).forGetter(ConsumableTemperatureComponent::maxTemperature)
                    )
                    .apply(instance, ConsumableTemperatureComponent::new)
    );

    @Override
    public void onConsume(World world, LivingEntity user, ItemStack stack, ConsumableComponent consumable) {
        double temperature = temperature() + random.nextDouble(
                (int) Math.round(minTemperature * 1000),
                (int) Math.round(maxTemperature * 1000)) * 0.001;

        if (temperature != 0) {
            TemperatureManager temperatureManager = ((LivingEntityAccess) user).thermia$getTemperatureManager();
            temperatureManager.modifyTemperature(temperature);
        }
    }

    private static final Random random = new Random();
}
