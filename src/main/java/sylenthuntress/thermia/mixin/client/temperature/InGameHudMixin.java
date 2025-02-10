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

@SuppressWarnings("DataFlowIssue")
@Mixin(InGameHud.class)
public class InGameHudMixin {
    @Shadow
    @Final
    private MinecraftClient client;

    @ModifyExpressionValue(method = "renderMiscOverlays", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;getFrozenTicks()I"))
    private int thermia$enableFrozenOverlay(int original) {
        return TemperatureHelper.getTemperatureManager(this.client.player).isHypothermic() ? 1 : original;
    }

    @ModifyExpressionValue(method = "renderMiscOverlays", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;getFreezingScale()F"))
    private float thermia$incrementFrozenOverlay(float original) {
        return TemperatureHelper.getTemperatureManager(this.client.player).isHypothermic()
                ? Math.min(1F, 0.01F + (0.99F * this.client.player.getStatusEffect(ThermiaStatusEffects.HYPOTHERMIA).getAmplifier() * 0.1F))
                : original;
    }
}
