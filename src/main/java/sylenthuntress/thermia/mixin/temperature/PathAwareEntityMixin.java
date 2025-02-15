package sylenthuntress.thermia.mixin.temperature;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import sylenthuntress.thermia.temperature.TemperatureHelper;

@Mixin(PathAwareEntity.class)
public class PathAwareEntityMixin {
    @ModifyReturnValue(
            method = "getPathfindingFavor(Lnet/minecraft/util/math/BlockPos;)F",
            at = @At("RETURN")
    )
    private float thermia$modifyFavorWithTemperature(float original, BlockPos pos) {
        LivingEntity entity = (LivingEntity) (Object) this;
        if (TemperatureHelper.lacksTemperature(entity)) {
            return original;
        }

        var temperatureManager = TemperatureHelper.getTemperatureManager(entity);
        double ambientTemperature = TemperatureHelper.getAmbientTemperature(entity.getWorld(), pos);
        float temperatureFavor = temperatureManager.distanceFromTemperateBounds(
                (ambientTemperature
                        + temperatureManager.getModifiedTemperature()) / 2
        ) * 0.1F;

        return temperatureManager.isHyperthermic() || temperatureManager.isHypothermic()
                ? original - temperatureFavor + 1
                : original - temperatureFavor;
    }
}
