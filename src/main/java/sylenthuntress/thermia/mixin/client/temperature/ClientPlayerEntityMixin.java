package sylenthuntress.thermia.mixin.client.temperature;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import sylenthuntress.thermia.temperature.TemperatureHelper;

@SuppressWarnings("ConstantValue")
@Mixin(ClientPlayerEntity.class)
public abstract class ClientPlayerEntityMixin {
    @ModifyReturnValue(method = "canSprint", at = @At("RETURN"))
    private boolean thermia$disableSprinting(boolean original) {
        return original && !TemperatureHelper.getTemperatureManager((PlayerEntity) (Object) this).isHyperthermic();
    }
}
