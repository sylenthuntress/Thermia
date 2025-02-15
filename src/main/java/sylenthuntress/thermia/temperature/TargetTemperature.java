package sylenthuntress.thermia.temperature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import sylenthuntress.thermia.registry.ThermiaAttachmentTypes;
import sylenthuntress.thermia.registry.ThermiaAttributes;
import sylenthuntress.thermia.registry.ThermiaTags;

@SuppressWarnings("UnstableApiUsage")
public record TargetTemperature(double value) {
    public final static TargetTemperature DEFAULT = new TargetTemperature(97);
    public static Codec<TargetTemperature> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.DOUBLE.fieldOf("target_temperature").forGetter(TargetTemperature::value)
    ).apply(instance, TargetTemperature::new));
    public static PacketCodec<ByteBuf, TargetTemperature> PACKET_CODEC = PacketCodecs.codec(CODEC);

    public TargetTemperature(LivingEntity entity) {
        this(entity.getAttributeValue(ThermiaAttributes.BASE_TEMPERATURE));
    }

    public static TargetTemperature setValue(double newTemperature) {
        return new TargetTemperature(newTemperature);
    }

    @SuppressWarnings({"UnusedReturnValue"})
    public static double calculateTargetTemperature(LivingEntity entity) {
        double targetTemperature;
        if (!TemperatureHelper.getTemperatureManager(entity).hasTemperature()
                || entity.getType().isIn(ThermiaTags.EntityType.CLIMATE_UNAFFECTED)) {
            targetTemperature = entity.getAttributeValue(ThermiaAttributes.BASE_TEMPERATURE);

            entity.setAttached(
                    ThermiaAttachmentTypes.TARGET_TEMPERATURE,
                    setValue(targetTemperature)
            );
            return targetTemperature;
        }

        double baseTemperature = entity.getAttributeValue(ThermiaAttributes.BASE_TEMPERATURE);
        double ambientTemperature = TemperatureHelper.getAmbientTemperature(entity.getWorld(), entity.getBlockPos());

        targetTemperature = (baseTemperature + ambientTemperature) / 2;
        
        entity.setAttached(
                ThermiaAttachmentTypes.TARGET_TEMPERATURE,
                setValue(targetTemperature)
        );
        return targetTemperature;
    }
}
