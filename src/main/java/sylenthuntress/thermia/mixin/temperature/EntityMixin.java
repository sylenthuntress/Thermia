package sylenthuntress.thermia.mixin.temperature;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import sylenthuntress.thermia.Thermia;
import sylenthuntress.thermia.temperature.TemperatureHelper;
import sylenthuntress.thermia.temperature.TemperatureModifier;

@Mixin(Entity.class)
public abstract class EntityMixin {
    @Shadow
    public abstract boolean isOnFire();

    @Shadow
    public abstract World getWorld();

    @Inject(method = "setOnFire", at = @At("HEAD"))
    private void thermia$setFireTemperature(boolean onFire, CallbackInfo ci) {
        if ((Entity) (Object) this instanceof LivingEntity livingEntity) {
            if (onFire)
                TemperatureHelper.getTemperatureManager(livingEntity).getTemperatureModifiers().addModifier(new TemperatureModifier(
                        Thermia.modIdentifier("on_fire"),
                        100,
                        TemperatureModifier.Operation.ADD_VALUE
                ));
            else
                TemperatureHelper.getTemperatureManager(livingEntity).getTemperatureModifiers().removeModifier(Thermia.modIdentifier("on_fire"));
        }
    }
}
