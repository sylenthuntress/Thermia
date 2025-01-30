package sylenthuntress.thermia.util;

import net.minecraft.entity.LivingEntity;
import net.minecraft.world.World;
import sylenthuntress.thermia.registry.ThermiaAttributes;

public abstract class TemperatureHelper {
    public static double getAmbientTemperature(World level, net.minecraft.util.math.BlockPos blockPos) {
        float biomeTemperature = level.getBiome(blockPos).value().getTemperature();
        return biomeTemperature;
    }

    public static double getTargetTemperature(LivingEntity entity) {
        double bodyTemperature = entity.getAttributeValue(ThermiaAttributes.BODY_TEMPERATURE);
        return (bodyTemperature + getAmbientTemperature(entity.getWorld(), entity.getBlockPos())) / 2;
    }
}
