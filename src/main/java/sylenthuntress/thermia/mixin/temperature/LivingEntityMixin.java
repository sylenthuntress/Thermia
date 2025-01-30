package sylenthuntress.thermia.mixin.temperature;

import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import sylenthuntress.thermia.Thermia;
import sylenthuntress.thermia.access.temperature.LivingEntityAccess;
import sylenthuntress.thermia.util.TemperatureHelper;
import sylenthuntress.thermia.util.TemperatureManager;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin implements LivingEntityAccess {
    @Unique
    private final TemperatureManager thermia$temperatureManager = new TemperatureManager((LivingEntity) (Object) this);

    @Inject(method = "tick", at = @At("HEAD"))
    private void thermia$calculateTemperature(CallbackInfo ci) {
        double targetTemperature = TemperatureHelper.getTargetTemperature((LivingEntity) (Object) this);
        if (thermia$temperatureManager.getTemperature() > targetTemperature)
            thermia$temperatureManager.modifyTemperature(0.1, 0);
        else thermia$temperatureManager.modifyTemperature(0, 0.1);
        Thermia.LOGGER.debug(""+ thermia$temperatureManager.getTemperature());
    }
}
