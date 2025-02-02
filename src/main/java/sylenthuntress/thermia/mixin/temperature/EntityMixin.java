package sylenthuntress.thermia.mixin.temperature;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import sylenthuntress.thermia.Thermia;
import sylenthuntress.thermia.registry.ThermiaEffects;
import sylenthuntress.thermia.temperature.TemperatureHelper;
import sylenthuntress.thermia.temperature.TemperatureModifier;

@Mixin(Entity.class)
public abstract class EntityMixin {
    @Shadow public abstract boolean isOnFire();

    @Shadow public abstract World getWorld();

    @ModifyReturnValue(method = "isFrozen", at = @At("RETURN"))
    private boolean thermia$setFrozen(boolean original) {
        if ((Entity) (Object) this instanceof LivingEntity livingEntity)
            original = original || livingEntity.hasStatusEffect(ThermiaEffects.HYPOTHERMIA);
        return original;
    }

    @Inject(method = "setOnFire", at = @At("HEAD"))
    private void thermia$setFireTemperature(boolean onFire, CallbackInfo ci) {
        if ((Entity) (Object) this instanceof LivingEntity livingEntity) {
            if (onFire)
                TemperatureHelper.addModifier(livingEntity, new TemperatureModifier(
                    Thermia.modIdentifier("on_fire"),
                    100,
                    TemperatureModifier.Operation.ADD_VALUE
            ));
            else
                TemperatureHelper.removeModifier(livingEntity, Thermia.modIdentifier("on_fire"));
        }
    }
}
