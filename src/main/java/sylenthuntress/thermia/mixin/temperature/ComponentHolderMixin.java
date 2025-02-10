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
import org.spongepowered.asm.mixin.injection.At;
import sylenthuntress.thermia.Thermia;
import sylenthuntress.thermia.registry.ThermiaAttributes;
import sylenthuntress.thermia.registry.ThermiaTags;

@Mixin(ComponentHolder.class)
public interface ComponentHolderMixin {
    @ModifyReturnValue(
            method = "getOrDefault",
            at = @At("RETURN")
    )
    private Object applyDefaultModifiers(Object obj) {
        //noinspection ConstantValue
        if (!((ComponentHolder) this instanceof ItemStack stack)
                || !(obj instanceof AttributeModifiersComponent component)) {
            return obj;
        }

        if (stack.contains(DataComponentTypes.EQUIPPABLE)
                && stack.isIn(ThermiaTags.Item.Equippable.INSULATING)) {

            final EquipmentSlot itemSlot = stack.get(
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

        if (!stack.hasEnchantments()) {
            return component;
        }

        for (RegistryEntry<Enchantment> enchantment : stack.getEnchantments().getEnchantments()) {
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
                                    2 + 0.25 * stack.getEnchantments().getLevel(enchantment),
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
                                    2 + 0.25 * stack.getEnchantments().getLevel(enchantment),
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
                                    -(2 + 0.25 * stack.getEnchantments().getLevel(enchantment)),
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
                                    2 + 0.25 * stack.getEnchantments().getLevel(enchantment),
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
