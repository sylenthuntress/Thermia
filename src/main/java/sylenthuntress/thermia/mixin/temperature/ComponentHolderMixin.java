package sylenthuntress.thermia.mixin.temperature;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.component.ComponentHolder;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import sylenthuntress.thermia.Thermia;
import sylenthuntress.thermia.registry.ThermiaAttributes;
import sylenthuntress.thermia.registry.ThermiaComponents;
import sylenthuntress.thermia.registry.ThermiaTags;
import sylenthuntress.thermia.registry.data_components.SunBlockingComponent;

@Mixin(ComponentHolder.class)
public interface ComponentHolderMixin {
    @ModifyReturnValue(
            method = "getOrDefault",
            at = @At("RETURN")
    )
    private Object thermia$applyDefaultModifiers$1(Object obj) {
        return thermia$calculateDefaultModifiers(obj);
    }

    @ModifyReturnValue(
            method = "get",
            at = @At("RETURN")
    )
    private Object thermia$applyDefaultModifiers$2(Object obj) {
        return thermia$calculateDefaultModifiers(obj);
    }

    @Unique
    default Object thermia$calculateDefaultModifiers(Object obj) {
        //noinspection ConstantValue
        if (!((ComponentHolder) this instanceof ItemStack stack)
                || !(obj instanceof AttributeModifiersComponent component)) {
            return obj;
        }

        // Apply default equippable attributes
        if (stack.contains(DataComponentTypes.EQUIPPABLE)) {
            if (stack.isIn(ThermiaTags.Item.Equippable.INSULATING)) {
                final EquipmentSlot itemSlot = stack.get(
                        DataComponentTypes.EQUIPPABLE
                ).slot();

                component = component.with(
                        ThermiaAttributes.COLD_OFFSET_THRESHOLD,
                        new EntityAttributeModifier(
                                Thermia.modIdentifier("cold_offset_modifier."
                                        + "."
                                        + itemSlot.asString()
                                ),
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
                                        + "."
                                        + itemSlot.asString()
                                ),
                                -0.2,
                                EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE
                        ),
                        AttributeModifierSlot.forEquipmentSlot(
                                itemSlot
                        )
                );
            }
            if (stack.isIn(ThermiaTags.Item.Equippable.BREEZY)) {
                final EquipmentSlot itemSlot = stack.get(
                        DataComponentTypes.EQUIPPABLE
                ).slot();

                component = component.with(
                        ThermiaAttributes.HEAT_OFFSET_THRESHOLD,
                        new EntityAttributeModifier(
                                Thermia.modIdentifier("heat_offset_modifier."
                                        + "."
                                        + itemSlot.asString()
                                ),
                                2,
                                EntityAttributeModifier.Operation.ADD_VALUE
                        ),
                        AttributeModifierSlot.forEquipmentSlot(
                                itemSlot
                        )
                );

                component = component.with(
                        ThermiaAttributes.COLD_OFFSET_THRESHOLD,
                        new EntityAttributeModifier(
                                Thermia.modIdentifier("cold_offset_modifier."
                                        + "."
                                        + itemSlot.asString()
                                ),
                                -0.2,
                                EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE
                        ),
                        AttributeModifierSlot.forEquipmentSlot(
                                itemSlot
                        )
                );
            }
        }

        // Apply default sun blocking attributes
        if (!stack.contains(ThermiaComponents.SUN_BLOCKING)
                && stack.isIn(ThermiaTags.Item.Equippable.BLOCKS_SUNLIGHT)) {
            if (stack.isIn(ThermiaTags.Item.Equippable.BlocksSunlight.ANY)) {
                stack.set(
                        ThermiaComponents.SUN_BLOCKING,
                        new SunBlockingComponent(
                                1,
                                AttributeModifierSlot.ANY
                        )
                );
            }
            if (stack.isIn(ThermiaTags.Item.Equippable.BlocksSunlight.BODY)) {
                stack.set(
                        ThermiaComponents.SUN_BLOCKING,
                        new SunBlockingComponent(
                                1,
                                AttributeModifierSlot.BODY
                        )
                );
            }
            if (stack.isIn(ThermiaTags.Item.Equippable.BlocksSunlight.FEET)) {
                stack.set(
                        ThermiaComponents.SUN_BLOCKING,
                        new SunBlockingComponent(
                                1,
                                AttributeModifierSlot.FEET
                        )
                );
            }
            if (stack.isIn(ThermiaTags.Item.Equippable.BlocksSunlight.HANDS)) {
                stack.set(
                        ThermiaComponents.SUN_BLOCKING,
                        new SunBlockingComponent(
                                1,
                                AttributeModifierSlot.HAND
                        )
                );
            }
            if (stack.isIn(ThermiaTags.Item.Equippable.BlocksSunlight.HEAD)) {
                stack.set(
                        ThermiaComponents.SUN_BLOCKING,
                        new SunBlockingComponent(
                                1,
                                AttributeModifierSlot.HEAD
                        )
                );
            }
            if (stack.isIn(ThermiaTags.Item.Equippable.BlocksSunlight.LEGS)) {
                stack.set(
                        ThermiaComponents.SUN_BLOCKING,
                        new SunBlockingComponent(
                                1,
                                AttributeModifierSlot.LEGS
                        )
                );
            }
            if (stack.isIn(ThermiaTags.Item.Equippable.BlocksSunlight.MAINHAND)) {
                stack.set(
                        ThermiaComponents.SUN_BLOCKING,
                        new SunBlockingComponent(
                                1,
                                AttributeModifierSlot.MAINHAND
                        )
                );
            }
            if (stack.isIn(ThermiaTags.Item.Equippable.BlocksSunlight.OFFHAND)) {
                stack.set(
                        ThermiaComponents.SUN_BLOCKING,
                        new SunBlockingComponent(
                                1,
                                AttributeModifierSlot.OFFHAND
                        )
                );
            }
        }

        // Guard-return for unenchanted items
        if (!stack.hasEnchantments()) {
            return component;
        }

        // Apply default enchantment attribute modifiers
        final var enchantments = stack.getEnchantments();
        for (RegistryEntry<Enchantment> enchantment : enchantments.getEnchantments()) {
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
                                    2 + 0.25 * enchantments.getLevel(enchantment),
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
                                    2 + 0.25 * enchantments.getLevel(enchantment),
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
