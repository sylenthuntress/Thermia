package sylenthuntress.thermia.mixin.temperature;

import net.minecraft.component.type.ConsumableComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import sylenthuntress.thermia.access.temperature.LivingEntityAccess;
import sylenthuntress.thermia.registry.ThermiaComponents;
import sylenthuntress.thermia.registry.ThermiaTags;
import sylenthuntress.thermia.temperature.TemperatureManager;

import java.util.Arrays;

@Mixin(ConsumableComponent.class)
public class ConsumableComponentMixin {
    @Inject(method = "finishConsumption", at = @At("HEAD"))
    private void thermia$defaultConsumableTemperature(World world, LivingEntity user, ItemStack stack, CallbackInfoReturnable<ItemStack> cir) {
        if (!stack.contains(ThermiaComponents.CONSUMABLE_TEMPERATURE_COMPONENT)) {
            double[] consumedTemperatures = {0, 0};
            if (stack.isIn(ThermiaTags.COLD_FOODS))
                consumedTemperatures[0] -= 2;
            if (stack.isIn(ThermiaTags.REFRESHING_FOODS))
                consumedTemperatures[0] -= 0.5;
            if (stack.isIn(ThermiaTags.WARM_FOODS))
                consumedTemperatures[1] += 0.5;
            if (stack.isIn(ThermiaTags.HOT_FOODS))
                consumedTemperatures[1] += 2;
            if (Arrays.stream(consumedTemperatures).anyMatch(value -> value != 0)) {
                TemperatureManager temperatureManager = ((LivingEntityAccess) user).thermia$getTemperatureManager();
                if (temperatureManager.isHypothermic())
                    consumedTemperatures[1] *= 4;
                else if (temperatureManager.isHyperthermic())
                        consumedTemperatures[0] *= 4;
                temperatureManager.modifyTemperature(consumedTemperatures);
            }
        }
    }
}
