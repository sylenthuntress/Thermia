package sylenthuntress.thermia.mixin.temperature;

import net.minecraft.component.ComponentHolder;
import net.minecraft.component.ComponentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.MergedComponentMap;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.item.consume.ApplyEffectsConsumeEffect;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.text.Text;
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
    @Shadow public abstract boolean isIn(TagKey<Item> tag);

    @Shadow @Nullable public abstract <T> T apply(ComponentType<T> type, T defaultValue, UnaryOperator<T> applier);

    @Shadow @Nullable public abstract <T> T set(ComponentType<? super T> type, @Nullable T value);

    @Shadow public abstract Text getItemName();

    @Shadow public abstract Item getItem();

    @Shadow public abstract boolean isIn(RegistryEntryList<Item> registryEntryList);

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
                    new ConsumableTemperatureComponent(calculateConsumableTemperatures())
            );

            if (this.isIn(ThermiaTags.Item.Consumable.APPLIES_FROST_RESISTANCE)) {
                //noinspection DataFlowIssue
                component.onConsumeEffects().add(
                        new ApplyEffectsConsumeEffect(
                                new StatusEffectInstance(
                                        ThermiaStatusEffects.FROST_RESISTANCE,
                                        6000
                                )
                        )
                );
            }
        }

        if (this.contains(DataComponentTypes.EQUIPPABLE)
                && this.isIn(ThermiaTags.Item.Equippable.INSULATING)) {
            //noinspection DataFlowIssue
            final EquipmentSlot itemSlot = this.get(
                    DataComponentTypes.EQUIPPABLE
            ).slot();

            this.apply(
                    DataComponentTypes.ATTRIBUTE_MODIFIERS,
                    AttributeModifiersComponent.DEFAULT,
                    component -> component.with(
                            ThermiaAttributes.COLD_OFFSET_THRESHOLD,
                            new EntityAttributeModifier(
                                    Thermia.modIdentifier("cold_offset_modifier." + itemSlot.asString()),
                                    2,
                                    EntityAttributeModifier.Operation.ADD_VALUE
                            ),
                            AttributeModifierSlot.forEquipmentSlot(
                                    itemSlot
                            )
                    )
            );
        }
    }

    @Unique
    private double[] calculateConsumableTemperatures() {
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
}
