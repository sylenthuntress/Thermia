package sylenthuntress.thermia.util;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.Colors;
import sylenthuntress.thermia.Thermia;
import sylenthuntress.thermia.access.temperature.LivingEntityAccess;
import sylenthuntress.thermia.registry.ThermiaAttributes;
import sylenthuntress.thermia.registry.ThermiaEffects;

import java.util.Arrays;

public class TemperatureManager {
    private final LivingEntity entity;
    private double temperature;

    public TemperatureManager(LivingEntity livingEntity) {
        entity = livingEntity;
        temperature = livingEntity.getAttributeValue(ThermiaAttributes.BODY_TEMPERATURE);
    }

    private double setTemperature(double newTemperature) {
        return temperature = newTemperature;
    }

    public double modifyTemperature(double... inputTemperatures) {
        double newTemperature = temperature;
        for (double inputTemperature : inputTemperatures) {
            double bodyTemperature = entity.getAttributeValue(ThermiaAttributes.BODY_TEMPERATURE);
            if (temperature > bodyTemperature && inputTemperature > 0)
                inputTemperature *= entity.getAttributeValue(ThermiaAttributes.HEAT_MODIFIER);
            else if (temperature < bodyTemperature && inputTemperature < 0)
                inputTemperature *= entity.getAttributeValue(ThermiaAttributes.COLD_MODIFIER);

            newTemperature += inputTemperature;
        }
        return setTemperature(newTemperature);
    }

    public double stepPassiveTemperature() {
        double inputTemperature = TemperatureHelper.getTargetTemperature(entity) - temperature;
        double newTemperature = modifyTemperature(inputTemperature * 0.0025);

        /*
         double[] extremeTemperatures = TemperatureHelper.getExtremeTemperatures(entity);
         if (Arrays.stream(extremeTemperatures).anyMatch((var) -> var > 0))
            newTemperature = modifyTemperature(extremeTemperatures);
        */

        applyStatus();

        return newTemperature;
    }

    public void applyStatus() {
        if (temperature < 95)
            entity.addStatusEffect(new StatusEffectInstance(
                    ThermiaEffects.HYPOTHERMIA,
                    (int) Math.round(213.4 - temperature) * 2,
                    0,
                    true,
                    false,
                    true
            ));
        else if (temperature >= 106.7)
            entity.addStatusEffect(new StatusEffectInstance(
                    ThermiaEffects.HYPERPYREXIA,
                    (int) Math.round(temperature) * 2,
                    0,
                    true,
                    false,
                    true
            ));
    }

    public double getTemperature() {
        return temperature;
    }
}
