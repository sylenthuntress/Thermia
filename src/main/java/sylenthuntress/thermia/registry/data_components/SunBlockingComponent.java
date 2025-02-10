package sylenthuntress.thermia.registry.data_components;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.util.dynamic.Codecs;

public record SunBlockingComponent(float amount, AttributeModifierSlot slot) {
    public static final SunBlockingComponent DEFAULT = new SunBlockingComponent(0, AttributeModifierSlot.ANY);

    public static final Codec<SunBlockingComponent> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            Codecs.POSITIVE_FLOAT.fieldOf("amount").forGetter(SunBlockingComponent::amount),
            AttributeModifierSlot.CODEC.optionalFieldOf("slot", AttributeModifierSlot.ANY).forGetter(SunBlockingComponent::slot)
    ).apply(builder, SunBlockingComponent::new));
}
