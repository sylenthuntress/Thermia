package sylenthuntress.thermia.temperature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import sylenthuntress.thermia.registry.ThermiaAttributes;

public record Temperature(double value) {
    public final static Temperature DEFAULT = new Temperature(97);
    public static Codec<Temperature> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.DOUBLE.fieldOf("temperature").forGetter(Temperature::value)
    ).apply(instance, Temperature::new));
    public static PacketCodec<ByteBuf, Temperature> PACKET_CODEC = PacketCodecs.codec(CODEC);

    public Temperature(LivingEntity entity) {
        this(entity.getAttributeValue(ThermiaAttributes.BASE_TEMPERATURE));
    }

    public static Temperature setValue(double newTemperature) {
        return new Temperature(newTemperature);
    }
}
