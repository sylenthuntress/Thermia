package sylenthuntress.thermia.temperature;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.registry.tag.FluidTags;
import sylenthuntress.thermia.Thermia;
import sylenthuntress.thermia.registry.ThermiaAttachmentTypes;
import sylenthuntress.thermia.registry.ThermiaAttributes;
import sylenthuntress.thermia.registry.ThermiaStatusEffects;

@SuppressWarnings("UnstableApiUsage")
public class TemperatureManager {
    private final LivingEntity entity;
    private final TemperatureModifierContainer modifiers = new TemperatureModifierContainer();

    public TemperatureManager(LivingEntity livingEntity) {
        entity = livingEntity;
        setTemperature(getTargetTemperature());
    }

    public double setTemperature(double newTemperature) {
        if (entity.isAlive()) {
            if (!entity.getWorld().isClient())
                return entity.setAttached(
                        ThermiaAttachmentTypes.TEMPERATURE,
                        Temperature.setValue(newTemperature)
                ).value();
            return entity.getAttached(ThermiaAttachmentTypes.TEMPERATURE).value();
        }
        return 0;
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
        return (bodyTemperature + ambientTemperature) / 2;
    }

    public double stepPassiveTemperature() {
        if (entity.isAlive()) {
            double inputTemperature = getTargetTemperature() - getTemperature();
            double newTemperature = modifyTemperature(inputTemperature * 0.0025);
            newTemperature += modifyTemperature(stepPassiveInteractions());

            if (entity.isInCreativeMode())
                applyStatus();

            return newTemperature;
        }
        return 0;
    }

    public double[] stepPassiveInteractions() {
        double[] interactionTemperatures = {0, 0};
        getTemperatureModifiers().removeModifiers(
                Thermia.modIdentifier("powder_snow"),
                Thermia.modIdentifier("on_fire"),
                Thermia.modIdentifier("lava")
        );
        if (entity.inPowderSnow) {
            interactionTemperatures[0] -= 0.05;
            getTemperatureModifiers().addModifier(new TemperatureModifier(
                    Thermia.modIdentifier("powder_snow"),
                    -10F,
                    TemperatureModifier.Operation.ADD_VALUE
                    )
            );
        }
        if (entity.isOnFire() && !entity.isFireImmune()) {
            interactionTemperatures[1] += 0.05;
            getTemperatureModifiers().addModifier(new TemperatureModifier(
                            Thermia.modIdentifier("on_fire"),
                            10F,
                            TemperatureModifier.Operation.ADD_VALUE
                    )
            );
        }
        if (entity.getBlockStateAtPos().getFluidState().isIn(FluidTags.LAVA)) {
            interactionTemperatures[1] += 0.1;
            getTemperatureModifiers().addModifier(new TemperatureModifier(
                            Thermia.modIdentifier("lava"),
                            30F,
                            TemperatureModifier.Operation.ADD_VALUE
                    )
            );
        }
        return interactionTemperatures;
    }

    public void applyStatus() {
        int amplifier = getHypothermiaAmplifier();
        if (amplifier >= 0) {
            entity.setStatusEffect(new StatusEffectInstance(
                    ThermiaStatusEffects.HYPOTHERMIA,
                    -1,
                    amplifier,
                    true,
                    false,
                    true
            ), null);
            entity.getStatusEffect(ThermiaStatusEffects.HYPOTHERMIA).onApplied(entity);
        } else if ((amplifier = getHyperthermiaAmplifier()) >= 0) {
            entity.setStatusEffect(new StatusEffectInstance(
                    ThermiaStatusEffects.HYPERTHERMIA,
                    -1,
                    amplifier,
                    true,
                    false,
                    true
            ), null);
            entity.getStatusEffect(ThermiaStatusEffects.HYPERTHERMIA).onApplied(entity);
        } else {
            entity.removeStatusEffect(ThermiaStatusEffects.HYPOTHERMIA);
            entity.removeStatusEffect(ThermiaStatusEffects.HYPERTHERMIA);
        }
    }

    public double getTemperature() {
        if (entity.isAlive())
            return entity.getAttachedOrCreate(
                ThermiaAttachmentTypes.TEMPERATURE,
                () -> new Temperature(entity.getAttributeValue(ThermiaAttributes.BODY_TEMPERATURE))).value();
        return 0;
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

    public boolean isHypothermic() {
        return entity.hasStatusEffect(ThermiaStatusEffects.HYPOTHERMIA);
    }

    public boolean isHyperthermic() {
        return entity.hasStatusEffect(ThermiaStatusEffects.HYPERTHERMIA);
    }

    public boolean shouldShake() {
        return getTargetTemperature() < (entity.getAttributeValue(ThermiaAttributes.BODY_TEMPERATURE) -
                entity.getAttributeBaseValue(ThermiaAttributes.COLD_OFFSET_THRESHOLD));
    }

    public boolean shouldBlurVision() {
        return isHyperthermic();
    }

    public boolean shouldDoWobble() {
        return getTargetTemperature() > (entity.getAttributeValue(ThermiaAttributes.BODY_TEMPERATURE) +
                entity.getAttributeBaseValue(ThermiaAttributes.HEAT_OFFSET_THRESHOLD));
    }

    public int getHypothermiaAmplifier() {
        double threshold = entity.getAttributeValue(ThermiaAttributes.BODY_TEMPERATURE)
                + entity.getAttributeBaseValue(ThermiaAttributes.COLD_OFFSET_THRESHOLD);
        int amplifier = -1;
        while (threshold > getModifiedTemperature() && amplifier < 256) {
            threshold += entity.getAttributeBaseValue(ThermiaAttributes.COLD_OFFSET_THRESHOLD);
            amplifier++;
        }
        return amplifier;
    }

    public int getHyperthermiaAmplifier() {
        double threshold = entity.getAttributeValue(ThermiaAttributes.BODY_TEMPERATURE)
                + entity.getAttributeBaseValue(ThermiaAttributes.HEAT_OFFSET_THRESHOLD);
        int amplifier = -1;
        while (threshold < getModifiedTemperature() && amplifier < 256) {
            threshold += entity.getAttributeBaseValue(ThermiaAttributes.HEAT_OFFSET_THRESHOLD);
            amplifier++;
        }
        return amplifier;
    }
}
