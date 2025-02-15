package sylenthuntress.thermia.mixin.temperature;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.entity.ai.goal.WanderAroundGoal;
import net.minecraft.entity.mob.PathAwareEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import sylenthuntress.thermia.temperature.TemperatureHelper;

@Mixin(WanderAroundGoal.class)
public class WanderAroundGoalMixin {
    @Shadow
    @Final
    protected PathAwareEntity mob;

    @ModifyExpressionValue(
            method = "canStart",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/entity/ai/goal/WanderAroundGoal;chance:I"
            )
    )
    private int thermia$increaseChanceWithTemperature(int original) {
        var temperatureManager = TemperatureHelper.getTemperatureManager(this.mob);
        float chanceMultiplier = Math.min(0.01F, 1 - temperatureManager.normalizeWithinTemperateBounds(
                temperatureManager.getModifiedTemperature()
        ));

        return (int) Math.clamp(original * chanceMultiplier, 1, original);
    }
}
