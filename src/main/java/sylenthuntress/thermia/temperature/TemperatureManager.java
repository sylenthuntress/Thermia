package sylenthuntress.thermia.temperature;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.registry.tag.FluidTags;
import sylenthuntress.thermia.Thermia;
import sylenthuntress.thermia.registry.ThermiaAttachmentTypes;
import sylenthuntress.thermia.registry.ThermiaAttributes;
import sylenthuntress.thermia.registry.ThermiaStatusEffects;
import sylenthuntress.thermia.registry.ThermiaTags;

@SuppressWarnings("UnstableApiUsage")
public class TemperatureManager {
    protected final LivingEntity entity;
    private final TemperatureModifierContainer modifiers = new TemperatureModifierContainer();

    public TemperatureManager(LivingEntity livingEntity) {
        entity = livingEntity;
    }

    @SuppressWarnings("DataFlowIssue")
    public double setTemperature(double newTemperature) {
        if (!hasTemperature()) {
            return 0;
        }

        if (!entity.getWorld().isClient()) {
            entity.setAttached(
                    ThermiaAttachmentTypes.TEMPERATURE,
                    Temperature.setValue(newTemperature)
            );
        }

        return entity.getAttached(ThermiaAttachmentTypes.TEMPERATURE).value();
    }

    public double modifyTemperature(double... inputTemperatures) {
        double newTemperature = getTemperature();
        if (hasTemperature())
            for (double inputTemperature : inputTemperatures)
               newTemperature += inputTemperature;
        return setTemperature(newTemperature);
    }

    public double getTargetTemperature() {
        return entity.getAttachedOrElse(ThermiaAttachmentTypes.TARGET_TEMPERATURE, TargetTemperature.DEFAULT).value();
    }

    public void stepPassiveTemperature() {
        if (!hasTemperature()) {
            return;
        }

        TargetTemperature.calculateTargetTemperature(entity);
        if (!entity.isInCreativeMode()) {
            double inputTemperature = getTargetTemperature() - getTemperature();
            modifyTemperature(inputTemperature * 0.0125);
            modifyTemperature(stepPassiveInteractions());
        }

        applyStatus();
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

    @SuppressWarnings("DataFlowIssue")
    public void applyStatus() {
        int amplifier = getHypothermiaAmplifier();
        if (amplifier >= 0) {
            final var effect = ThermiaStatusEffects.HYPOTHERMIA;

            if (!entity.hasStatusEffect(effect) || entity.getStatusEffect(effect).isInfinite()) {
                entity.setStatusEffect(new StatusEffectInstance(
                        effect,
                        -1,
                        amplifier,
                        true,
                        false,
                        true
                ), null);
                entity.getStatusEffect(effect).onApplied(entity);
            }
        }
        else if ((amplifier = getHyperthermiaAmplifier()) >= 0) {
            final var effect = ThermiaStatusEffects.HYPERTHERMIA;

            if (!entity.hasStatusEffect(effect) || entity.getStatusEffect(effect).isInfinite()) {
                entity.setStatusEffect(new StatusEffectInstance(
                        effect,
                        -1,
                        amplifier,
                        true,
                        false,
                        true
                ), null);
                entity.getStatusEffect(effect).onApplied(entity);
            }
        }
        else {
            var effect = ThermiaStatusEffects.HYPOTHERMIA;
            if (entity.hasStatusEffect(effect) && entity.getStatusEffect(effect).isInfinite())
                entity.removeStatusEffect(effect);

            effect = ThermiaStatusEffects.HYPERTHERMIA;
            if (entity.hasStatusEffect(effect) && entity.getStatusEffect(effect).isInfinite())
                entity.removeStatusEffect(effect);
        }
    }

    public double getTemperature() {
        if (hasTemperature())
            return entity.getAttachedOrCreate(
                    ThermiaAttachmentTypes.TEMPERATURE,
                    () -> new Temperature(entity.getAttributeValue(ThermiaAttributes.BASE_TEMPERATURE))).value();
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

    public boolean hasTemperature() {
        return entity.isAlive()
                && !entity.getType().isIn(ThermiaTags.EntityType.TEMPERATURE_IMMUNE);
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

    public boolean doColdEffects() {
        return isHypothermic() ||
                getTargetTemperature() < (entity.getAttributeValue(ThermiaAttributes.BASE_TEMPERATURE) -
                        entity.getAttributeBaseValue(ThermiaAttributes.COLD_OFFSET_THRESHOLD))
                && !entity.isInCreativeMode()
                && !entity.isSpectator();
    }

    public boolean shouldBlurVision() {
        return isHyperthermic();
    }

    public boolean doHeatEffects() {
        return isHyperthermic() ||
                getTargetTemperature() > (entity.getAttributeValue(ThermiaAttributes.BASE_TEMPERATURE) +
                        entity.getAttributeBaseValue(ThermiaAttributes.HEAT_OFFSET_THRESHOLD))
                && !entity.isInCreativeMode()
                && !entity.isSpectator();
    }

    public int getHypothermiaAmplifier() {
        double threshold = entity.getAttributeValue(ThermiaAttributes.BASE_TEMPERATURE)
                - entity.getAttributeBaseValue(ThermiaAttributes.COLD_OFFSET_THRESHOLD);
        int amplifier = -1;

        while (threshold > getModifiedTemperature() && amplifier < 256) {
            threshold -= entity.getAttributeBaseValue(ThermiaAttributes.COLD_OFFSET_THRESHOLD);
            amplifier++;
        }

        return amplifier;
    }

    public int getHyperthermiaAmplifier() {
        double threshold = entity.getAttributeValue(ThermiaAttributes.BASE_TEMPERATURE)
                + entity.getAttributeBaseValue(ThermiaAttributes.HEAT_OFFSET_THRESHOLD);
        int amplifier = -1;

        while (threshold < getModifiedTemperature() && amplifier < 256) {
            threshold += entity.getAttributeBaseValue(ThermiaAttributes.HEAT_OFFSET_THRESHOLD);
            amplifier++;
        }

        return amplifier;
    }
}
