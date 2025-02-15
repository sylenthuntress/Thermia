package sylenthuntress.thermia.event;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.world.ServerWorld;
import sylenthuntress.thermia.registry.ThermiaAttributes;
import sylenthuntress.thermia.temperature.TemperatureHelper;

public class CalculateBaseTemperature implements ServerEntityEvents.Load {
    @Override
    public void onLoad(Entity entity, ServerWorld world) {
        if (!(entity instanceof LivingEntity livingEntity) || entity.isPlayer()) {
            return;
        }

        final var temperatureManager = TemperatureHelper.getTemperatureManager(entity);
        double baseTemperature = temperatureManager.getBaseTemperature();

        // Guard-return if following calculations have already been done
        if (baseTemperature != ThermiaAttributes.BASE_TEMPERATURE.value().getDefaultValue()) {
            return;
        }

        double regionalTemperature = TemperatureHelper.getRegionalTemperature(entity.getWorld(), entity.getBlockPos());
        baseTemperature = (baseTemperature + regionalTemperature) / 2;

        var attributeInstance = livingEntity.getAttributes().getCustomInstance(ThermiaAttributes.BASE_TEMPERATURE);
        if (attributeInstance != null) {
            attributeInstance.setBaseValue(baseTemperature);
        }
    }
}
