package sylenthuntress.thermia.temperature;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import sylenthuntress.thermia.registry.ThermiaAttachmentTypes;
import sylenthuntress.thermia.registry.ThermiaAttributes;

import java.util.ArrayList;

@SuppressWarnings("UnstableApiUsage")
public class TemperatureManager {
    private final LivingEntity entity;
    private ArrayList<TemperatureModifier> temperatureModifiers;

    public TemperatureManager(LivingEntity livingEntity) {
        entity = livingEntity;
        temperatureModifiers = new ArrayList<>();
    }

    public double setTemperature(double newTemperature) {
        if (!entity.getWorld().isClient())
            return entity.setAttached(ThermiaAttachmentTypes.TEMPERATURE, Temperature.setValue(newTemperature)).value();
        return entity.getAttached(ThermiaAttachmentTypes.TEMPERATURE).value();
    }

    public double modifyTemperature(boolean applyModifiers, double... inputTemperatures) {
        double newTemperature = getTemperature();
        for (double inputTemperature : inputTemperatures) {
            double targetTemperature = TemperatureHelper.getTargetTemperature(entity);
            double bodyTemperature = entity.getAttributeValue(ThermiaAttributes.BODY_TEMPERATURE);
            if (applyModifiers) {
                if (targetTemperature < bodyTemperature && inputTemperature > 0)
                    inputTemperature *= entity.getAttributeValue(ThermiaAttributes.HEAT_MODIFIER);
                else if (bodyTemperature > targetTemperature && inputTemperature < 0)
                    inputTemperature *= entity.getAttributeValue(ThermiaAttributes.COLD_MODIFIER);
            }
            newTemperature += inputTemperature;
        }
        return setTemperature(newTemperature);
    }

    public double modifyTemperature(double... inputTemperatures) {
        return modifyTemperature(true, inputTemperatures);
    }

    public double stepPassiveTemperature() {
        double inputTemperature = TemperatureHelper.getTargetTemperature(entity) - getTemperature();
        double newTemperature = modifyTemperature(inputTemperature * 0.0025);

        applyStatus();

        return newTemperature;
    }

    public void applyStatus() {

    }

    public double getTemperature() {
        return entity.getAttachedOrCreate(
                ThermiaAttachmentTypes.TEMPERATURE,
                () -> new Temperature(entity.getAttributeValue(ThermiaAttributes.BODY_TEMPERATURE))).value();
    }

    public double getModifiedTemperature() {
        double temperature = getTemperature();
        for (TemperatureModifier modifier : getTemperatureModifiers()) {
            switch (modifier.operation()) {
                case ADD_VALUE -> temperature += modifier.value();
                case ADD_MULTIPLIED_VALUE -> temperature += temperature * modifier.value();
            }
        }
        return temperature;
    }

    public ArrayList<TemperatureModifier> getTemperatureModifiers() {
        return temperatureModifiers;
    }
}
