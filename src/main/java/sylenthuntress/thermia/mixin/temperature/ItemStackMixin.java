package sylenthuntress.thermia.mixin.temperature;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import sylenthuntress.thermia.access.temperature.LivingEntityAccess;
import sylenthuntress.thermia.registry.ThermiaComponents;
import sylenthuntress.thermia.registry.ThermiaTags;
import sylenthuntress.thermia.registry.data_components.ConsumableTemperatureComponent;
import sylenthuntress.thermia.temperature.TemperatureManager;

import java.util.Arrays;

@Mixin(ItemStack.class)
public class ItemStackMixin {
    @ModifyVariable(
            method = "<init>(Lnet/minecraft/item/ItemConvertible;ILnet/minecraft/component/MergedComponentMap;)V",
            at = @At("HEAD"),
            argsOnly = true)
    private static ItemStack thermia$applyDefaultModifiers(ItemStack stack) {
        if (!stack.contains(DataComponentTypes.CONSUMABLE) && !stack.contains(DataComponentTypes.EQUIPPABLE)) {
            return stack;
        }

        if (stack.contains(DataComponentTypes.CONSUMABLE) &&
                !stack.contains(ThermiaComponents.CONSUMABLE_TEMPERATURE_COMPONENT)) {
            double[] temperatures = {0, 0, 0};

            if (stack.isIn(ThermiaTags.Item.COLD_FOODS)) {
                temperatures[0] -= 2;
            }
            if (stack.isIn(ThermiaTags.Item.REFRESHING_FOODS)) {
                temperatures[0] -= 0.5;
            }
            if (stack.isIn(ThermiaTags.Item.WARM_FOODS)) {
                temperatures[0] += 0.5;
            }
            if (stack.isIn(ThermiaTags.Item.HOT_FOODS)) {
                temperatures[0] += 2;
            }

            stack.set(
                    ThermiaComponents.CONSUMABLE_TEMPERATURE_COMPONENT,
                    new ConsumableTemperatureComponent(temperatures)
            );
        }

        return stack;
    }
}
