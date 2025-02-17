package sylenthuntress.thermia.data.predicate;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.predicate.NumberRange;
import net.minecraft.predicate.entity.EntitySubPredicate;
import sylenthuntress.thermia.temperature.TemperatureHelper;

public record EntityTemperaturePredicate(
        NumberRange.DoubleRange baseTemperature,
        NumberRange.DoubleRange currentTemperature,
        NumberRange.DoubleRange targetTemperature,
        NumberRange.DoubleRange unmodifiedTemperature
) {
    public static final Codec<EntityTemperaturePredicate> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                            NumberRange.DoubleRange.CODEC.optionalFieldOf("base_temperature", NumberRange.DoubleRange.ANY)
                                    .forGetter(EntityTemperaturePredicate::baseTemperature),
                            NumberRange.DoubleRange.CODEC.optionalFieldOf("current_temperature", NumberRange.DoubleRange.ANY)
                                    .forGetter(EntityTemperaturePredicate::currentTemperature),
                            NumberRange.DoubleRange.CODEC.optionalFieldOf("target_temperature", NumberRange.DoubleRange.ANY)
                                    .forGetter(EntityTemperaturePredicate::targetTemperature),
                            NumberRange.DoubleRange.CODEC.optionalFieldOf("unmodified_temperature", NumberRange.DoubleRange.ANY)
                                    .forGetter(EntityTemperaturePredicate::unmodifiedTemperature)
                    )
                    .apply(instance, EntityTemperaturePredicate::new)
    );

    public MapCodec<? extends EntitySubPredicate> getCodec() {
        return null;
    }

    public boolean test(Entity entity) {
        if (TemperatureHelper.lacksTemperature(entity)) {
            return false;
        }

        final var temperatureManager = TemperatureHelper.getTemperatureManager(entity);

        if (!baseTemperature.test(temperatureManager.getBaseTemperature())) {
            return false;
        } else if (!currentTemperature.test(temperatureManager.getModifiedTemperature())) {
            return false;
        } else if (!targetTemperature.test(temperatureManager.getTargetTemperature())) {
            return false;
        } else return unmodifiedTemperature.test(temperatureManager.getTemperature());
    }

    @SuppressWarnings("unused")
    public static class Builder {
        private NumberRange.DoubleRange baseTemperature;
        private NumberRange.DoubleRange currentTemperature;
        private NumberRange.DoubleRange targetTemperature;
        private NumberRange.DoubleRange unmodifiedTemperature;

        public static EntityTemperaturePredicate.Builder create() {
            return new EntityTemperaturePredicate.Builder();
        }

        public EntityTemperaturePredicate.Builder setBaseTemperature(NumberRange.DoubleRange baseTemperature) {
            this.baseTemperature = baseTemperature;
            return this;
        }

        public EntityTemperaturePredicate.Builder setCurrentTemperature(NumberRange.DoubleRange currentTemperature) {
            this.currentTemperature = currentTemperature;
            return this;
        }

        public EntityTemperaturePredicate.Builder setTargetTemperature(NumberRange.DoubleRange targetTemperature) {
            this.targetTemperature = targetTemperature;
            return this;
        }

        public EntityTemperaturePredicate.Builder setUnmodifiedTemperature(NumberRange.DoubleRange unmodifiedTemperature) {
            this.unmodifiedTemperature = unmodifiedTemperature;
            return this;
        }

        public EntityTemperaturePredicate build() {
            return new EntityTemperaturePredicate(this.baseTemperature, this.currentTemperature, this.targetTemperature, this.unmodifiedTemperature);
        }
    }
}
