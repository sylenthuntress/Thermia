package sylenthuntress.thermia.temperature;

import io.wispforest.owo.config.ConfigSynchronizer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.server.network.ServerPlayerEntity;
import sylenthuntress.thermia.Thermia;
import sylenthuntress.thermia.data.ThermiaTags;
import sylenthuntress.thermia.registry.ThermiaAttachmentTypes;
import sylenthuntress.thermia.registry.ThermiaAttributes;
import sylenthuntress.thermia.registry.ThermiaCriteria;
import sylenthuntress.thermia.registry.ThermiaStatusEffects;

@SuppressWarnings("UnstableApiUsage")
public class TemperatureManager {
    protected final LivingEntity entity;
    private final TemperatureModifierContainer modifiers = new TemperatureModifierContainer();

    public TemperatureManager(LivingEntity livingEntity) {
        entity = livingEntity;
    }

    public double setTemperature(double newTemperature) {
        if (!canHaveTemperature() || entity.getWorld().isClient()) {
            return entity.getAttachedOrCreate(
                    ThermiaAttachmentTypes.TEMPERATURE,
                    () -> new Temperature(entity)
            ).value();
        }

        entity.setAttached(
                ThermiaAttachmentTypes.TEMPERATURE,
                Temperature.setValue(newTemperature)
        );

        if (entity instanceof ServerPlayerEntity player) {
            ThermiaCriteria.TEMPERATURE_CHANGED.trigger(player, newTemperature, false);
            ThermiaCriteria.TEMPERATURE_CHANGED.trigger(player, getTemperatureModifiers().withModifiers(newTemperature), true);
        }

        return newTemperature;
    }

    public double modifyTemperature(double... inputTemperatures) {
        double newTemperature = getTemperature();
        if (canHaveTemperature())
            for (double inputTemperature : inputTemperatures)
               newTemperature += inputTemperature;

        return setTemperature(newTemperature);
    }

    public double getTargetTemperature() {
        return entity.getAttachedOrElse(
                ThermiaAttachmentTypes.TARGET_TEMPERATURE,
                new TargetTemperature(entity)
        ).value();
    }

    public void stepPassiveTemperature() {
        if (!canHaveTemperature()) {
            return;
        }

        TargetTemperature.calculateTargetTemperature(entity);
        double inputTemperature = getTargetTemperature() - getTemperature();
        modifyTemperature(inputTemperature * 0.0125);
        modifyTemperature(stepPassiveInteractions());

        applyStatus();
    }

    public double[] stepPassiveInteractions() {
        double[] interactionTemperatures = {0, 0};
        getTemperatureModifiers().removeModifiers(
                Thermia.modIdentifier("granted/powder_snow"),
                Thermia.modIdentifier("granted/on_fire"),
                Thermia.modIdentifier("granted/lava")
        );
        if (entity.inPowderSnow) {
            interactionTemperatures[0] -= 0.05;
            getTemperatureModifiers().addModifier(new TemperatureModifier(
                    Thermia.modIdentifier("granted/powder_snow"),
                            -10F,
                            TemperatureModifier.Operation.ADD_VALUE
                    )
            );
        }
        if (entity.isOnFire() && !entity.isFireImmune()) {
            interactionTemperatures[1] += 0.05;
            getTemperatureModifiers().addModifier(new TemperatureModifier(
                    Thermia.modIdentifier("granted/on_fire"),
                            10F,
                            TemperatureModifier.Operation.ADD_VALUE
                    )
            );
        }
        if (entity.getBlockStateAtPos().getFluidState().isIn(FluidTags.LAVA)) {
            interactionTemperatures[1] += 0.1;
            getTemperatureModifiers().addModifier(new TemperatureModifier(
                    Thermia.modIdentifier("granted/lava"),
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
            boolean showIcon = entity instanceof ServerPlayerEntity player
                    && (boolean) ConfigSynchronizer.getClientOptions(
                    player,
                    "thermia-config"
            ).get(Thermia.CONFIG.keys.climateEffectDisplay_SHOW_HYPOTHERMIA);

            if (!entity.hasStatusEffect(effect) || entity.getStatusEffect(effect).isInfinite()) {
                entity.setStatusEffect(new StatusEffectInstance(
                        effect,
                        -1,
                        amplifier,
                        true,
                        false,
                        showIcon
                ), null);
                entity.getStatusEffect(effect).onApplied(entity);

                if (entity instanceof ServerPlayerEntity player) {
                    ThermiaCriteria.PLAYER_FROZEN.trigger(player, amplifier);
                }
            }
        }
        else if ((amplifier = getHyperthermiaAmplifier()) >= 0) {
            final var effect = ThermiaStatusEffects.HYPERTHERMIA;
            boolean showIcon = entity instanceof ServerPlayerEntity player
                    && (boolean) ConfigSynchronizer.getClientOptions(
                    player,
                    "thermia-config"
            ).get(Thermia.CONFIG.keys.climateEffectDisplay_SHOW_HYPERTHERMIA);

            if (!entity.hasStatusEffect(effect) || entity.getStatusEffect(effect).isInfinite()) {
                entity.setStatusEffect(new StatusEffectInstance(
                        effect,
                        -1,
                        amplifier,
                        true,
                        false,
                        showIcon
                ), null);
                entity.getStatusEffect(effect).onApplied(entity);

                if (entity instanceof ServerPlayerEntity player) {
                    ThermiaCriteria.PLAYER_OVERHEATING.trigger(player, amplifier);
                }
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
        if (!hasTemperature()) {
            return entity.getAttributeValue(ThermiaAttributes.BASE_TEMPERATURE);
        }

        return entity.getAttachedOrCreate(
                ThermiaAttachmentTypes.TEMPERATURE,
                () -> new Temperature(entity)
        ).value();
    }

    public double getBaseTemperature() {
        return entity.getAttributeValue(ThermiaAttributes.BASE_TEMPERATURE);
    }

    public double getModifiedTemperature() {
        if (!hasTemperature()) {
            return entity.getAttributeValue(ThermiaAttributes.BASE_TEMPERATURE);
        }

        return getTemperatureModifiers().withModifiers(getTemperature());
    }

    public float distanceFromTemperateBounds(double temperature) {
        double clampedTemperature = Math.clamp(
                temperature,
                getBaseTemperature() - entity.getAttributeValue(ThermiaAttributes.COLD_OFFSET_THRESHOLD),
                getBaseTemperature() + entity.getAttributeValue(ThermiaAttributes.HEAT_OFFSET_THRESHOLD)
        );

        return (float) (temperature - clampedTemperature);
    }

    public float normalizeWithinTemperateBounds(double temperature) {
        return normalizeWithinTemperateBounds(temperature, getBaseTemperature());
    }

    public float normalizeWithinTemperateBounds(double temperature, double baseTemperature) {
        double clampedTemperature = Math.clamp(
                temperature,
                baseTemperature - entity.getAttributeValue(ThermiaAttributes.COLD_OFFSET_THRESHOLD),
                baseTemperature + entity.getAttributeValue(ThermiaAttributes.HEAT_OFFSET_THRESHOLD)
        );

        return (float) Math.abs(1 - temperature / clampedTemperature);
    }

    public boolean canHaveTemperature() {
        return entity.isAlive()
                && !entity.getType().isIn(ThermiaTags.EntityType.TEMPERATURE_IMMUNE);
    }

    public boolean hasTemperature() {
        return !(entity.hasStatusEffect(ThermiaStatusEffects.THERMOREGULATION)
                || entity.isSpectator()
                || entity.isInCreativeMode())
                && canHaveTemperature();
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
                        entity.getAttributeValue(ThermiaAttributes.COLD_OFFSET_THRESHOLD))
                        && hasTemperature();
    }

    public boolean shouldBlurVision() {
        return isHyperthermic();
    }

    public boolean doHeatEffects() {
        return isHyperthermic() ||
                getTargetTemperature() > (entity.getAttributeValue(ThermiaAttributes.BASE_TEMPERATURE) +
                        entity.getAttributeValue(ThermiaAttributes.HEAT_OFFSET_THRESHOLD))
                        && hasTemperature();
    }

    public int getHypothermiaAmplifier() {
        if (entity.hasStatusEffect(ThermiaStatusEffects.FROST_RESISTANCE)
                || !entity.canFreeze()
                || !Thermia.CONFIG.entityTemperature.CAN_FREEZE()) {
            return -1;
        }

        double threshold = entity.getAttributeValue(ThermiaAttributes.BASE_TEMPERATURE)
                - entity.getAttributeValue(ThermiaAttributes.COLD_OFFSET_THRESHOLD);
        int amplifier = -1;

        while (threshold > getModifiedTemperature() && amplifier < 256) {
            threshold -= entity.getAttributeValue(ThermiaAttributes.COLD_OFFSET_THRESHOLD);
            amplifier++;
        }

        return amplifier;
    }

    public int getHyperthermiaAmplifier() {
        if (entity.hasStatusEffect(StatusEffects.FIRE_RESISTANCE)
                || entity.isFireImmune()
                || !Thermia.CONFIG.entityTemperature.CAN_OVERHEAT()) {
            return -1;
        }

        double threshold = entity.getAttributeValue(ThermiaAttributes.BASE_TEMPERATURE)
                + entity.getAttributeValue(ThermiaAttributes.HEAT_OFFSET_THRESHOLD);
        int amplifier = -1;

        while (threshold < getModifiedTemperature() && amplifier < 256) {
            threshold += entity.getAttributeValue(ThermiaAttributes.HEAT_OFFSET_THRESHOLD);
            amplifier++;
        }

        return amplifier;
    }
}
