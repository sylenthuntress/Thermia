package sylenthuntress.thermia.mixin.client.temperature;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import sylenthuntress.thermia.temperature.TemperatureHelper;

@Mixin(InGameHud.HeartType.class)
public class InGameHudHeartTypeMixin {
    @ModifyExpressionValue(method = "fromPlayerState", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;isFrozen()Z"))
    private static boolean thermia$setFrozenHearts(boolean original, PlayerEntity player) {
        return original || TemperatureHelper.getTemperatureManager(player).isHypothermic();
    }
}
