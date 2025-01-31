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

    private double setTemperature(double newTemperature) {
        temperature = newTemperature;
        return temperature;
    }

    public double modifyTemperature(double coldTemperature, double hotTemperature) {
        double newTemperature = temperature;
        newTemperature -= coldTemperature;
        newTemperature += hotTemperature;
        return setTemperature(newTemperature);
    }

    public double stepPassiveTemperature() {
        double newTemperature = TemperatureHelper.getTargetTemperature(entity);
        double hotTemperature = Math.max(0, newTemperature - temperature) * -2;
        double coldTemperature = Math.min(0, temperature - newTemperature);
        return modifyTemperature(coldTemperature, hotTemperature);
    }

    public double getTemperature() {
        return temperature;
    }
}
