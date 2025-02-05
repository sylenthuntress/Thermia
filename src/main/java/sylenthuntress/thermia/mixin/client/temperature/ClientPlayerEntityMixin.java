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

@Mixin(ClientPlayerEntity.class)
public abstract class ClientPlayerEntityMixin {
    @Shadow protected abstract void tickNausea(boolean fromPortalEffect);

    @Shadow public float nauseaIntensity;

    @Shadow public float prevNauseaIntensity;

    @Inject(method = "tickMovement", at = @At("HEAD"))
    private void thermia$wobbleVision(CallbackInfo ci) {
        if (TemperatureHelper.getTemperatureManager((PlayerEntity) (Object) this).shouldDoWobble())
            this.nauseaIntensity = Math.max(nauseaIntensity, 0.1F);
    }

    @ModifyExpressionValue(method = "removeStatusEffectInternal", at = @At(value = "INVOKE", target = "Lnet/minecraft/registry/entry/RegistryEntry;matches(Lnet/minecraft/registry/entry/RegistryEntry;)Z"))
    private boolean thermia$removeHyperthermiaWobble(boolean original) {
        return original || (nauseaIntensity <= 0.1F
                && TemperatureHelper.getTemperatureManager((PlayerEntity) (Object) this).shouldDoWobble());
    }

    @ModifyReturnValue(method = "canSprint", at = @At("RETURN"))
    private boolean thermia$disableSprinting(boolean original) {
        return original && !TemperatureHelper.getTemperatureManager((PlayerEntity) (Object) this).shouldDoWobble();
    }
}
