package sylenthuntress.thermia.temperature;

import net.minecraft.entity.LivingEntity;
import sylenthuntress.thermia.registry.ThermiaAttachmentTypes;
import sylenthuntress.thermia.registry.ThermiaAttributes;

@SuppressWarnings("UnstableApiUsage")
public class TemperatureManager {
    private final LivingEntity entity;
    private final TemperatureModifierContainer modifiers = new TemperatureModifierContainer();

    public TemperatureManager(LivingEntity livingEntity) {
        entity = livingEntity;
    }

    public double setTemperature(double newTemperature) {
        if (!entity.getWorld().isClient())
            return entity.setAttached(ThermiaAttachmentTypes.TEMPERATURE, Temperature.setValue(newTemperature)).value();
        return entity.getAttached(ThermiaAttachmentTypes.TEMPERATURE).value();
    }

    public double modifyTemperature(double... inputTemperatures) {
        double newTemperature = getTemperature();
        for (double inputTemperature : inputTemperatures)
            newTemperature += inputTemperature;
        return setTemperature(newTemperature);
    }

    public double getTargetTemperature() {
        double bodyTemperature = entity.getAttributeValue(ThermiaAttributes.BODY_TEMPERATURE);
        double ambientTemperature = TemperatureHelper.getAmbientTemperature(entity.getWorld(), entity.getBlockPos());

        double targetTemperature;
        if (ambientTemperature > bodyTemperature)
            targetTemperature = (ambientTemperature + getTemperature())
                    * entity.getAttributeValue(ThermiaAttributes.HEAT_MODIFIER);
        else
            targetTemperature = (ambientTemperature + getTemperature())
                    * entity.getAttributeValue(ThermiaAttributes.COLD_MODIFIER);
        return targetTemperature / 2;
    }

    public double stepPassiveTemperature() {
        double inputTemperature = getTargetTemperature();
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
        for (TemperatureModifier modifier : getTemperatureModifiers().getList()) {
            switch (modifier.operation()) {
                case ADD_VALUE -> temperature += modifier.value();
                case ADD_MULTIPLIED_VALUE -> temperature += temperature * modifier.value();
            }
        }
        return temperature;
    }

    public TemperatureModifierContainer getTemperatureModifiers() {
        return modifiers;
    }
}
