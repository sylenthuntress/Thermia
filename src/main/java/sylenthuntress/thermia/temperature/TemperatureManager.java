package sylenthuntress.thermia.temperature;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import sylenthuntress.thermia.registry.ThermiaAttachmentTypes;
import sylenthuntress.thermia.registry.ThermiaAttributes;
import sylenthuntress.thermia.registry.ThermiaStatusEffects;

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
        return entity.setAttached(ThermiaAttachmentTypes.TEMPERATURE, Temperature.setValue(newTemperature)).value();
    }

    public double modifyTemperature(boolean applyModifiers, double... inputTemperatures) {
        double newTemperature = getTemperature();
        for (double inputTemperature : inputTemperatures) {
            double bodyTemperature = entity.getAttributeValue(ThermiaAttributes.BODY_TEMPERATURE);
            if (applyModifiers) {
                if (getTemperature() > bodyTemperature && inputTemperature > 0)
                    inputTemperature *= entity.getAttributeValue(ThermiaAttributes.HEAT_MODIFIER);
                else if (getTemperature() < bodyTemperature && inputTemperature < 0)
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
        if (getTemperature() < 95)
            entity.addStatusEffect(new StatusEffectInstance(
                    ThermiaStatusEffects.HYPOTHERMIA,
                    200,
                    0,
                    true,
                    false,
                    true
            ));
        else if (getTemperature() >= 106.7)
            entity.addStatusEffect(new StatusEffectInstance(
                    ThermiaStatusEffects.HYPERPYREXIA,
                    200,
                    0,
                    true,
                    false,
                    true
            ));
    }

    public double getTemperature() {
        return entity.getAttachedOrCreate(
                ThermiaAttachmentTypes.TEMPERATURE,
                () -> new Temperature(entity.getAttributeValue(ThermiaAttributes.BODY_TEMPERATURE))).value();
    }

    public ArrayList<TemperatureModifier> getTemperatureModifiers() {
        return temperatureModifiers;
    }
}
