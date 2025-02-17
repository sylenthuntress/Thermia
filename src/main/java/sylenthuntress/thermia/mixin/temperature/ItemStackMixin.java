package sylenthuntress.thermia.mixin.temperature;

import net.minecraft.component.ComponentHolder;
import net.minecraft.component.ComponentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.MergedComponentMap;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.item.consume.ApplyEffectsConsumeEffect;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.TagKey;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import sylenthuntress.thermia.Thermia;
import sylenthuntress.thermia.data.ThermiaTags;
import sylenthuntress.thermia.registry.ThermiaComponents;
import sylenthuntress.thermia.registry.ThermiaStatusEffects;
import sylenthuntress.thermia.registry.data_components.ConsumableTemperatureComponent;
import sylenthuntress.thermia.registry.data_components.TemperatureModifiersComponent;
import sylenthuntress.thermia.temperature.TemperatureModifier;

import java.util.Arrays;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin implements ComponentHolder {
    @Shadow
    public abstract boolean isIn(TagKey<Item> tag);

    @Shadow
    @Nullable
    public abstract <T> T set(ComponentType<? super T> type, @Nullable T value);

    @Shadow
    public abstract boolean hasEnchantments();

    @Shadow
    public abstract ItemEnchantmentsComponent getEnchantments();

    @Inject(
            method = "<init>(Lnet/minecraft/item/ItemConvertible;ILnet/minecraft/component/MergedComponentMap;)V",
            at = @At("TAIL")
    )
    private void thermia$applyDefaultComponents(ItemConvertible item, int count, MergedComponentMap components, CallbackInfo ci) {
        if (!Thermia.SERVER_LOADED) {
            return;
        }

        final TemperatureModifiersComponent modifiers = this.getOrDefault(
                ThermiaComponents.TEMPERATURE_MODIFIERS,
                TemperatureModifiersComponent.DEFAULT
        );
        thermia$calculateTemperatureModifiers(modifiers);

        if (!this.contains(DataComponentTypes.CONSUMABLE)) {
            return;
        }

        if (this.contains(DataComponentTypes.CONSUMABLE)
                && !this.contains(ThermiaComponents.CONSUMABLE_TEMPERATURE)) {
            final var component = this.get(DataComponentTypes.CONSUMABLE);
            final double[] degrees = thermia$calculateConsumableTemperatures();

            if (Arrays.stream(degrees).anyMatch(value -> value != 0)) {
                this.set(
                        ThermiaComponents.CONSUMABLE_TEMPERATURE,
                        new ConsumableTemperatureComponent(degrees)
                );
            }

            if (this.isIn(ThermiaTags.Item.Consumable.APPLIES_FROST_RESISTANCE)) {
                @SuppressWarnings("DataFlowIssue") final var consumeEffects = component.onConsumeEffects();

                int duration = consumeEffects.stream().mapToInt(consumeEffect -> {
                    if (consumeEffect instanceof ApplyEffectsConsumeEffect consumeStatusEffect) {
                        return consumeStatusEffect.effects().stream().mapToInt(effect
                                -> effect.getEffectType() == StatusEffects.FIRE_RESISTANCE
                                ? effect.getDuration()
                                : 0
                        ).sum();
                    }

                    return 0;
                }).sum();

                if (duration == 0) {
                    duration = 1200;
                }

                consumeEffects.add(
                        new ApplyEffectsConsumeEffect(
                                new StatusEffectInstance(
                                        ThermiaStatusEffects.FROST_RESISTANCE,
                                        duration
                                )
                        )
                );
            }
        }
    }

    @Unique
    private double[] thermia$calculateConsumableTemperatures() {
        double[] temperatures = {0, 0, 0};

        if (this.isIn(ThermiaTags.Item.Consumable.COLD_FOODS)) {
            temperatures[0] -= 2;
        }
        if (this.isIn(ThermiaTags.Item.Consumable.REFRESHING_FOODS)) {
            temperatures[0] -= 0.5;
        }
        if (this.isIn(ThermiaTags.Item.Consumable.WARM_FOODS)) {
            temperatures[0] += 0.5;
        }
        if (this.isIn(ThermiaTags.Item.Consumable.HOT_FOODS)) {
            temperatures[0] += 2;
        }

        return temperatures;
    }

    @Unique
    private void thermia$calculateTemperatureModifiers(TemperatureModifiersComponent component) {
        if (this.isIn(ThermiaTags.Item.Equippable.COLD_WHEN_HELD)) {
            this.set(
                    ThermiaComponents.TEMPERATURE_MODIFIERS,
                    component.with(
                            new TemperatureModifier(
                                    Thermia.modIdentifier("cold_when_held"),
                                    -1,
                                    TemperatureModifier.Operation.ADD_VALUE
                            ),
                            AttributeModifierSlot.HAND
                    )
            );
        }
        if (this.isIn(ThermiaTags.Item.Equippable.HOT_WHEN_HELD)) {
            this.set(
                    ThermiaComponents.TEMPERATURE_MODIFIERS,
                    component.with(
                            new TemperatureModifier(
                                    Thermia.modIdentifier("hot_when_held"),
                                    1,
                                    TemperatureModifier.Operation.ADD_VALUE
                            ),
                            AttributeModifierSlot.HAND
                    )
            );
        }

        // Guard-return for unenchanted items
        if (!this.hasEnchantments()) {
            return;
        }

        // Apply default enchantment temperature modifiers
        final var enchantments = this.getEnchantments();
        for (RegistryEntry<Enchantment> enchantment : enchantments.getEnchantments()) {
            for (AttributeModifierSlot slot : enchantment.value().definition().slots()) {
                if (enchantment.isIn(ThermiaTags.Enchantment.PROVIDES_CHILL)) {
                    this.set(
                            ThermiaComponents.TEMPERATURE_MODIFIERS,
                            component.with(
                                    new TemperatureModifier(
                                            Thermia.modIdentifier(
                                                    "enchantment."
                                                            + enchantment.getIdAsString()
                                                            .replaceFirst("[A-Za-z0-9]+:", "")
                                                            + ".chill"
                                            ),
                                            -(2 + 0.25 * enchantments.getLevel(enchantment)),
                                            TemperatureModifier.Operation.ADD_VALUE
                                    ),
                                    slot
                            )
                    );
                }

                if (enchantment.isIn(ThermiaTags.Enchantment.PROVIDES_WARMTH)) {
                    this.set(
                            ThermiaComponents.TEMPERATURE_MODIFIERS,
                            component.with(
                                    new TemperatureModifier(
                                            Thermia.modIdentifier(
                                                    "enchantment."
                                                            + enchantment.getIdAsString()
                                                            .replaceFirst("[A-Za-z0-9]+:", "")
                                                            + ".warmth"
                                            ),
                                            2 + 0.25 * enchantments.getLevel(enchantment),
                                            TemperatureModifier.Operation.ADD_VALUE
                                    ),
                                    slot
                            )
                    );
                }
            }
        }
    }
}
