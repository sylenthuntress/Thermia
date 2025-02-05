package sylenthuntress.thermia.mixin.temperature;

import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import sylenthuntress.thermia.temperature.TemperatureHelper;

@Mixin(PlayerEntity.class)
public class PlayerEntityMixin {
    @ModifyVariable(method = "addExhaustion", at = @At("HEAD"), argsOnly = true)
    private float modifyExhaustion(float exhaustion) {
        if (TemperatureHelper.getTemperatureManager((PlayerEntity) (Object) this).isHyperthermic())
            exhaustion *= 2;
        return exhaustion;
    }
}
