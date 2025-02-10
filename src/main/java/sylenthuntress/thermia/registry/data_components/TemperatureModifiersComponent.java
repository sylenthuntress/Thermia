package sylenthuntress.thermia.registry.data_components;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import sylenthuntress.thermia.temperature.TemperatureModifier;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public record TemperatureModifiersComponent(List<Entry> modifiers, boolean showInTooltip) {
    public static final TemperatureModifiersComponent DEFAULT = new TemperatureModifiersComponent(List.of(), true);
    public static final DecimalFormat DECIMAL_FORMAT = Util.make(
            new DecimalFormat("#.##"), format -> format.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(Locale.ROOT))
    );
    private static final Codec<TemperatureModifiersComponent> BASE_CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                            TemperatureModifiersComponent.Entry.CODEC.listOf().fieldOf("modifiers").forGetter(TemperatureModifiersComponent::modifiers),
                            Codec.BOOL.optionalFieldOf("show_in_tooltip", true).forGetter(TemperatureModifiersComponent::showInTooltip)
                    )
                    .apply(instance, TemperatureModifiersComponent::new)
    );
    public static final Codec<TemperatureModifiersComponent> CODEC = Codec.withAlternative(
            BASE_CODEC, TemperatureModifiersComponent.Entry.CODEC.listOf(), entries
                    -> new TemperatureModifiersComponent(entries, true)
    );

    public TemperatureModifiersComponent with(TemperatureModifier modifier, AttributeModifierSlot slot) {
        if (hasModifier(modifier.id())) {
            return this;
        }

        ArrayList<Entry> newModifiers = new ArrayList<>(modifiers);

        newModifiers.add(
                new Entry(
                        modifier,
                        slot
                )
        );

        return new TemperatureModifiersComponent(newModifiers, this.showInTooltip);
    }

    public boolean hasModifier(Identifier id) {
        return modifiers.stream().anyMatch((entry -> entry.modifier().idMatches(id)));
    }

    public record Entry(TemperatureModifier modifier, AttributeModifierSlot slot) {
        public static final Codec<TemperatureModifiersComponent.Entry> CODEC = RecordCodecBuilder.create(
                instance -> instance.group(
                                TemperatureModifier.MAP_CODEC.forGetter(TemperatureModifiersComponent.Entry::modifier),
                                AttributeModifierSlot.CODEC.optionalFieldOf("slot", AttributeModifierSlot.ANY).forGetter(TemperatureModifiersComponent.Entry::slot)
                        )
                        .apply(instance, TemperatureModifiersComponent.Entry::new)
        );
    }
}
