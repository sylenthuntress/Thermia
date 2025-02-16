package sylenthuntress.thermia.mixin.client.temperature;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.component.ComponentHolder;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import sylenthuntress.thermia.Thermia;
import sylenthuntress.thermia.registry.ThermiaAttributes;
import sylenthuntress.thermia.registry.ThermiaComponents;
import sylenthuntress.thermia.registry.data_components.TemperatureModifiersComponent;
import sylenthuntress.thermia.temperature.TemperatureModifier;

import java.util.function.Consumer;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin implements ComponentHolder {
    @Unique
    protected int slotIndex0 = -1;
    @Unique
    protected int slotIndex1 = -1;

    @SuppressWarnings("DataFlowIssue")
    @ModifyExpressionValue(
            method = "applyAttributeModifier",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/item/ItemStack;getOrDefault(Lnet/minecraft/component/ComponentType;Ljava/lang/Object;)Ljava/lang/Object;"
            )
    )
    private Object thermia$allowTemperatureModifiers(Object obj) {
        var component = (AttributeModifiersComponent) obj;

        // Guard if no temperature modifiers are found on stack, or if they're hidden
        if (!this.contains(ThermiaComponents.TEMPERATURE_MODIFIERS)
                || !this.get(ThermiaComponents.TEMPERATURE_MODIFIERS).showInTooltip()) {
            return component;
        }

        // Add temperature modifiers as faux attribute modifiers
        for (TemperatureModifiersComponent.Entry entry
                : this.get(ThermiaComponents.TEMPERATURE_MODIFIERS).modifiers()) {
            component = component.with(
                    ThermiaAttributes.BASE_TEMPERATURE,
                    new EntityAttributeModifier(
                            // Suffix is used to identify itself as a faux attribute
                            entry.modifier().id().withSuffixedPath(".temperature_modifier"),
                            entry.modifier().amount(),
                            entry.modifier().operation().asAttributeOperation()
                    ),
                    entry.slot()
            );
        }

        slotIndex0 = -1;
        slotIndex1 = -1;
        return component;
    }

    @SuppressWarnings("DataFlowIssue")
    @ModifyExpressionValue(
            method = "appendAttributeModifiersTooltip",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/component/type/AttributeModifiersComponent;showInTooltip()Z"
            )
    )
    private boolean thermia$allowTemperatureModifiers(boolean original) {
        if (this.contains(ThermiaComponents.TEMPERATURE_MODIFIERS)) {
            return original || this.get(ThermiaComponents.TEMPERATURE_MODIFIERS).showInTooltip();
        }

        return original;
    }

    @SuppressWarnings("DataFlowIssue")
    @WrapOperation(
            method = "method_57370",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/item/ItemStack;appendAttributeModifierTooltip(Ljava/util/function/Consumer;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/registry/entry/RegistryEntry;Lnet/minecraft/entity/attribute/EntityAttributeModifier;)V"
            )
    )
    private void thermia$applyTemperatureModifiers(
            ItemStack instance,
            Consumer<Text> consumer,
            @Nullable PlayerEntity player,
            RegistryEntry<EntityAttribute> attribute,
            EntityAttributeModifier modifier,
            Operation<Void> original,
            @Local(argsOnly = true) AttributeModifierSlot modifierSlot) {
        // Guard in case of regular attribute modifiers
        if (!modifier.id().toString().endsWith(".temperature_modifier")) {
            // Prevent showing hidden attribute modifiers
            if (!instance.get(DataComponentTypes.ATTRIBUTE_MODIFIERS).showInTooltip()) {
                return;
            }

            original.call(instance, consumer, player, attribute, modifier);
            return;
        }

        // Prevent similar tooltips from doubling up
        if (modifier.operation().ordinal() == 0) {
            if (slotIndex0 == modifierSlot.ordinal()) {
                return;
            }
            slotIndex0 += 1;
        } else if (modifier.operation().ordinal() == 1) {
            if (slotIndex1 == modifierSlot.ordinal()) {
                return;
            }
            slotIndex1 += 1;
        }

        // Combine similar modifiers into one tooltip
        double amount = 0;
        for (TemperatureModifiersComponent.Entry entry : instance.get(ThermiaComponents.TEMPERATURE_MODIFIERS).modifiers()) {
            if (entry.slot() != modifierSlot
                    || entry.modifier().operation().asAttributeOperation() != modifier.operation()) {
                continue;
            }

            amount += entry.modifier().amount();
        }

        String temperatureScale = "temperature.scale.fahrenheit";
        switch (Thermia.CONFIG.temperatureScaleDisplay()) {
            case CELSIUS -> {
                amount *= 5.0 / 9.0;
                temperatureScale = "temperature.scale.celsius";
            }
            case KELVIN -> {
                amount *= 5.0 / 9.0;
                temperatureScale = "temperature.scale.kelvin";
            }
        }

        // Change the display amount based on operation
        final double displayAmount;
        if (modifier.operation().ordinal() == 1) {
            displayAmount = amount * 100.0;
        } else if (modifier.operation().ordinal() == 2) {
            consumer.accept(
                    Text.translatable(
                            "temperature.modifier.new",
                            amount
                    ).formatted(Formatting.BLUE)
            );
            return;
        } else {
            displayAmount = amount;
        }

        // Finally apply tooltip
        if (amount > 0.0) {
            consumer.accept(
                    Text.translatable(
                            "temperature.modifier.hot."
                                    + TemperatureModifier.Operation.asTemperatureOperation(modifier.operation())
                                    .getId(),
                            TemperatureModifiersComponent.DECIMAL_FORMAT.format(displayAmount)
                    ).append(
                            Text.translatable(temperatureScale)
                    ).append(
                            Text.translatable("temperature.symbol.fire", " ")
                    ).formatted(Formatting.GOLD)
            );
        } else {
            consumer.accept(
                    Text.translatable(
                            "temperature.modifier.cold."
                                    + TemperatureModifier.Operation.asTemperatureOperation(modifier.operation())
                                    .getId(),
                            TemperatureModifiersComponent.DECIMAL_FORMAT.format(-displayAmount)
                    ).append(
                            Text.translatable(temperatureScale)
                    ).append(
                            Text.translatable("temperature.symbol.snowflake", " ")
                    ).formatted(Formatting.AQUA)
            );
        }
    }
}
