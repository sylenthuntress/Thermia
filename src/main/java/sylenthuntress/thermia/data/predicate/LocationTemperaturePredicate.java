package sylenthuntress.thermia.data.predicate;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.predicate.NumberRange;
import net.minecraft.predicate.entity.EntitySubPredicate;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import sylenthuntress.thermia.temperature.TemperatureHelper;

public record LocationTemperaturePredicate(
        NumberRange.DoubleRange ambientTemperature,
        NumberRange.DoubleRange blockTemperature,
        NumberRange.DoubleRange fluidTemperature,
        NumberRange.DoubleRange regionalTemperature,
        NumberRange.DoubleRange seasonalTemperature
) {
    public static final Codec<LocationTemperaturePredicate> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                            NumberRange.DoubleRange.CODEC.optionalFieldOf("ambient_temperature", NumberRange.DoubleRange.ANY)
                                    .forGetter(LocationTemperaturePredicate::ambientTemperature),
                            NumberRange.DoubleRange.CODEC.optionalFieldOf("block_temperature", NumberRange.DoubleRange.ANY)
                                    .forGetter(LocationTemperaturePredicate::blockTemperature),
                            NumberRange.DoubleRange.CODEC.optionalFieldOf("fluid_temperature", NumberRange.DoubleRange.ANY)
                                    .forGetter(LocationTemperaturePredicate::fluidTemperature),
                            NumberRange.DoubleRange.CODEC.optionalFieldOf("regional_temperature", NumberRange.DoubleRange.ANY)
                                    .forGetter(LocationTemperaturePredicate::regionalTemperature),
                            NumberRange.DoubleRange.CODEC.optionalFieldOf("seasonal_temperature", NumberRange.DoubleRange.ANY)
                                    .forGetter(LocationTemperaturePredicate::seasonalTemperature)
                    )
                    .apply(instance, LocationTemperaturePredicate::new)
    );

    public MapCodec<? extends EntitySubPredicate> getCodec() {
        return null;
    }

    public boolean test(World world, BlockPos pos) {
        if (!ambientTemperature().test(TemperatureHelper.getAmbientTemperature(world, pos))) {
            return false;
        } else if (!blockTemperature().test(TemperatureHelper.getBlockTemperature(world, pos))) {
            return false;
        } else if (!fluidTemperature().test(TemperatureHelper.getFluidTemperature(world, pos))) {
            return false;
        } else if (!regionalTemperature().test(TemperatureHelper.getRegionalTemperature(world, pos))) {
            return false;
        } else return seasonalTemperature().test(TemperatureHelper.getSeasonalTemperature(world));
    }

    @SuppressWarnings("unused")
    public static class Builder {
        private NumberRange.DoubleRange ambientTemperature;
        private NumberRange.DoubleRange blockTemperature;
        private NumberRange.DoubleRange fluidTemperature;
        private NumberRange.DoubleRange regionalTemperature;
        private NumberRange.DoubleRange seasonalTemperature;

        public static LocationTemperaturePredicate.Builder create() {
            return new LocationTemperaturePredicate.Builder();
        }

        public LocationTemperaturePredicate.Builder setAmbientTemperature(NumberRange.DoubleRange ambientTemperature) {
            this.ambientTemperature = ambientTemperature;
            return this;
        }

        public LocationTemperaturePredicate.Builder setBlockTemperature(NumberRange.DoubleRange blockTemperature) {
            this.blockTemperature = blockTemperature;
            return this;
        }

        public LocationTemperaturePredicate.Builder setFluidTemperature(NumberRange.DoubleRange fluidTemperature) {
            this.fluidTemperature = fluidTemperature;
            return this;
        }

        public LocationTemperaturePredicate.Builder setRegionalTemperature(NumberRange.DoubleRange regionalTemperature) {
            this.regionalTemperature = regionalTemperature;
            return this;
        }

        public LocationTemperaturePredicate.Builder setSeasonalTemperature(NumberRange.DoubleRange seasonalTemperature) {
            this.seasonalTemperature = seasonalTemperature;
            return this;
        }

        public LocationTemperaturePredicate build() {
            return new LocationTemperaturePredicate(this.ambientTemperature, this.blockTemperature, this.fluidTemperature, this.regionalTemperature, this.seasonalTemperature);
        }
    }
}
