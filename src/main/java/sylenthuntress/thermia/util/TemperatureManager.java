package sylenthuntress.thermia.util;

import net.minecraft.entity.LivingEntity;
import sylenthuntress.thermia.registry.ThermiaAttributes;

public class TemperatureManager {
    private final LivingEntity entity;
    private double temperature;
    
    public TemperatureManager(LivingEntity livingEntity) {
        entity = livingEntity;
        temperature = livingEntity.getAttributeValue(ThermiaAttributes.BODY_TEMPERATURE);
    }

    public double modifyTemperature(double coldTemperature, double hotTemperature) {
        double newTemperature = temperature;
        newTemperature -= coldTemperature;
        newTemperature += hotTemperature;
        return newTemperature;
    }

    public double getTemperature() {
        return temperature;
    }
}
