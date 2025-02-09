package sylenthuntress.thermia.mixin.temperature;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.component.ComponentHolder;
import net.minecraft.component.ComponentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.MergedComponentMap;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttributeModifier;
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
import sylenthuntress.thermia.registry.ThermiaAttributes;
import sylenthuntress.thermia.registry.ThermiaComponents;
import sylenthuntress.thermia.registry.ThermiaStatusEffects;
import sylenthuntress.thermia.registry.ThermiaTags;
import sylenthuntress.thermia.registry.data_components.ConsumableTemperatureComponent;

import java.util.function.UnaryOperator;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin implements ComponentHolder {
    @Shadow
    public abstract boolean isIn(TagKey<Item> tag);

    @Shadow
    @Nullable
    public abstract <T> T apply(ComponentType<T> type, T defaultValue, UnaryOperator<T> applier);

    @Shadow
    @Nullable
    public abstract <T> T set(ComponentType<? super T> type, @Nullable T value);

    @Shadow
    public abstract ItemEnchantmentsComponent getEnchantments();

    @Shadow
    public abstract boolean hasEnchantments();

    @Inject(
            method = "<init>(Lnet/minecraft/item/ItemConvertible;ILnet/minecraft/component/MergedComponentMap;)V",
            at = @At("TAIL")
    )
    private void thermia$applyDefaultComponents(ItemConvertible item, int count, MergedComponentMap components, CallbackInfo ci) {
        if (!this.contains(DataComponentTypes.CONSUMABLE) && !this.contains(DataComponentTypes.EQUIPPABLE)) {
            return;
        }

        if (this.contains(DataComponentTypes.CONSUMABLE)
                && !this.contains(ThermiaComponents.CONSUMABLE_TEMPERATURE_COMPONENT)) {
            final var component = this.get(DataComponentTypes.CONSUMABLE);

            this.set(
                    ThermiaComponents.CONSUMABLE_TEMPERATURE_COMPONENT,
                    new ConsumableTemperatureComponent(thermia$calculateConsumableTemperatures())
            );

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

    @ModifyExpressionValue(
            method = "applyAttributeModifier",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/item/ItemStack;getOrDefault(Lnet/minecraft/component/ComponentType;Ljava/lang/Object;)Ljava/lang/Object;"
            )
    )
    private Object thermia$applyEnchantmentFallback$modifier(Object original) {
        return thermia$recalculateModifiers(original);
    }

    @ModifyExpressionValue(
            method = "applyAttributeModifiers",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/item/ItemStack;getOrDefault(Lnet/minecraft/component/ComponentType;Ljava/lang/Object;)Ljava/lang/Object;"
            )
    )
    private Object thermia$applyEnchantmentFallback$modifiers(Object original) {
        return thermia$recalculateModifiers(original);
    }

    @ModifyExpressionValue(
            method = "appendAttributeModifiersTooltip",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/item/ItemStack;getOrDefault(Lnet/minecraft/component/ComponentType;Ljava/lang/Object;)Ljava/lang/Object;"
            )
    )
    private Object thermia$applyEnchantmentFallback$tooltip(Object original) {
        return thermia$recalculateModifiers(original);
    }

    @Unique
    private AttributeModifiersComponent thermia$recalculateModifiers(Object obj) {
        AttributeModifiersComponent component = (AttributeModifiersComponent) obj;

        if (this.contains(DataComponentTypes.EQUIPPABLE)
                && this.isIn(ThermiaTags.Item.Equippable.INSULATING)) {
            //noinspection DataFlowIssue
            final EquipmentSlot itemSlot = this.get(
                    DataComponentTypes.EQUIPPABLE
            ).slot();

            component = component.with(
                    ThermiaAttributes.COLD_OFFSET_THRESHOLD,
                    new EntityAttributeModifier(
                            Thermia.modIdentifier("cold_offset_modifier."
                                    + itemSlot.asString()),
                            2,
                            EntityAttributeModifier.Operation.ADD_VALUE
                    ),
                    AttributeModifierSlot.forEquipmentSlot(
                            itemSlot
                    )
            );

            component = component.with(
                    ThermiaAttributes.HEAT_OFFSET_THRESHOLD,
                    new EntityAttributeModifier(
                            Thermia.modIdentifier("heat_offset_modifier."
                                    + itemSlot.asString()),
                            -0.2,
                            EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE
                    ),
                    AttributeModifierSlot.forEquipmentSlot(
                            itemSlot
                    )
            );
        }

        if (!this.hasEnchantments()) {
            return component;
        }

        for (RegistryEntry<Enchantment> enchantment : this.getEnchantments().getEnchantments()) {
            for (AttributeModifierSlot slot : enchantment.value().definition().slots()) {
                if (enchantment.isIn(ThermiaTags.Enchantment.HYPERTHERMIA_PROTECTION)) {
                    component = component.with(
                            ThermiaAttributes.HEAT_OFFSET_THRESHOLD,
                            new EntityAttributeModifier(
                                    Thermia.modIdentifier(
                                            "enchantment."
                                                    + enchantment.getIdAsString()
                                                    .replaceFirst("[A-Za-z0-9]+:", "")
                                                    + ".heat_offset_threshold"
                                    ),
                                    2 + 0.25 * this.getEnchantments().getLevel(enchantment),
                                    EntityAttributeModifier.Operation.ADD_VALUE
                            ),
                            slot
                    );
                }

                if (enchantment.isIn(ThermiaTags.Enchantment.HYPOTHERMIA_PROTECTION)) {
                    component = component.with(
                            ThermiaAttributes.COLD_OFFSET_THRESHOLD,
                            new EntityAttributeModifier(
                                    Thermia.modIdentifier(
                                            "enchantment."
                                                    + enchantment.getIdAsString()
                                                    .replaceFirst("[A-Za-z0-9]+:", "")
                                                    + ".cold_offset_threshold"
                                    ),
                                    2 + 0.25 * this.getEnchantments().getLevel(enchantment),
                                    EntityAttributeModifier.Operation.ADD_VALUE
                            ),
                            slot
                    );
                }

                if (enchantment.isIn(ThermiaTags.Enchantment.PROVIDES_CHILL)) {
                    component = component.with(
                            ThermiaAttributes.BASE_TEMPERATURE,
                            new EntityAttributeModifier(
                                    Thermia.modIdentifier(
                                            "enchantment."
                                                    + enchantment.getIdAsString()
                                                    .replaceFirst("[A-Za-z0-9]+:", "")
                                                    + ".chill"
                                    ),
                                    -(2 + 0.25 * this.getEnchantments().getLevel(enchantment)),
                                    EntityAttributeModifier.Operation.ADD_VALUE
                            ),
                            slot
                    );
                }

                if (enchantment.isIn(ThermiaTags.Enchantment.PROVIDES_WARMTH)) {
                    component = component.with(
                            ThermiaAttributes.BASE_TEMPERATURE,
                            new EntityAttributeModifier(
                                    Thermia.modIdentifier(
                                            "enchantment."
                                                    + enchantment.getIdAsString()
                                                    .replaceFirst("[A-Za-z0-9]+:", "")
                                                    + ".warmth"
                                    ),
                                    2 + 0.25 * this.getEnchantments().getLevel(enchantment),
                                    EntityAttributeModifier.Operation.ADD_VALUE
                            ),
                            slot
                    );
                }
            }
        }

        return component;
    }
}
