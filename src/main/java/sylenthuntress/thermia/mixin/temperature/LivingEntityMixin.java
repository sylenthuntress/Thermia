package sylenthuntress.thermia.mixin.temperature;

import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import sylenthuntress.thermia.Thermia;
import sylenthuntress.thermia.access.temperature.LivingEntityAccess;
import sylenthuntress.thermia.util.TemperatureHelper;
import sylenthuntress.thermia.util.TemperatureManager;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin implements LivingEntityAccess {
    private final TemperatureManager temperatureManager = new TemperatureManager((LivingEntity) (Object) this);

    @Inject(method = "tick", at = @At("HEAD"))
    private void thermia$calculateTemperature(CallbackInfo ci) {
        double targetTemperature = TemperatureHelper.getTargetTemperature((LivingEntity) (Object) this);
        if (temperatureManager.getTemperature() > targetTemperature)
            temperatureManager.modifyTemperature(0.1, 0);
        else temperatureManager.modifyTemperature(0, 0.1);
        Thermia.LOGGER.debug(""+temperatureManager.getTemperature());
    }
}
