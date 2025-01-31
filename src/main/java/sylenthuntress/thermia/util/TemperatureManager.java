package sylenthuntress.thermia.util;

import net.minecraft.entity.LivingEntity;
import sylenthuntress.thermia.registry.ThermiaAttributes;

import java.util.Arrays;

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

    public double modifyTemperature(double... inputTemperatures) {
        double newTemperature = temperature;
        for (double inputTemperature : inputTemperatures)
            newTemperature += inputTemperature;
        return setTemperature(newTemperature);
    }

    public double stepPassiveTemperature() {
        double inputTemperature = TemperatureHelper.getTargetTemperature(entity) - temperature;
        double newTemperature = modifyTemperature(inputTemperature * 0.001);

        double[] extremeTemperatures = TemperatureHelper.getExtremeTemperatures(entity);
        if (Arrays.stream(extremeTemperatures).anyMatch((var) -> var > 0))
            newTemperature = modifyTemperature(extremeTemperatures);

        return newTemperature;
    }

    public double getTemperature() {
        return temperature;
    }
}
