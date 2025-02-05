package sylenthuntress.thermia.mixin.client.temperature;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.InGameHud;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import sylenthuntress.thermia.registry.ThermiaStatusEffects;
import sylenthuntress.thermia.temperature.TemperatureHelper;

@Mixin(InGameHud.class)
public class InGameHudMixin {
    @Shadow @Final private MinecraftClient client;

    @ModifyExpressionValue(method = "renderMiscOverlays", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;getFrozenTicks()I"))
    private int thermia$enableFrozenOverlay(int original) {
        return TemperatureHelper.getTemperatureManager(this.client.player).isHypothermic() ? 1 : original;
    }

    @ModifyExpressionValue(method = "renderMiscOverlays", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;getFreezingScale()F"))
    private float thermia$maxFrozenOverlay(float original) {
        return TemperatureHelper.getTemperatureManager(this.client.player).isHypothermic()
                ? 0.5F * (1 + this.client.player.getStatusEffect(ThermiaStatusEffects.HYPOTHERMIA).getAmplifier() * 0.1F)
                : original;
    }

    @ModifyExpressionValue(method = "renderMiscOverlays", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;hasStatusEffect(Lnet/minecraft/registry/entry/RegistryEntry;)Z"))
    private boolean thermia$disablePortalOverlay(boolean original) {
        return original || TemperatureHelper.getTemperatureManager(this.client.player).shouldBlurVision();
    }
}
