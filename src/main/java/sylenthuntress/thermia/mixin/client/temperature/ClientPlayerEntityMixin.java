package sylenthuntress.thermia.mixin.client.temperature;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import sylenthuntress.thermia.temperature.TemperatureHelper;

@SuppressWarnings("ConstantValue")
@Mixin(ClientPlayerEntity.class)
public abstract class ClientPlayerEntityMixin {
    @ModifyReturnValue(method = "canSprint", at = @At("RETURN"))
    private boolean thermia$disableSprinting(boolean original) {
        return original && !TemperatureHelper.getTemperatureManager((PlayerEntity) (Object) this).isHyperthermic();
    }
}
