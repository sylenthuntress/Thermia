package sylenthuntress.thermia.util;

import net.fabricmc.fabric.api.tag.convention.v2.ConventionalBiomeTags;
import net.minecraft.entity.LivingEntity;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import sylenthuntress.thermia.registry.ThermiaAttributes;

public abstract class TemperatureHelper {
    public static double getAmbientTemperature(World world, BlockPos blockPos) {
        float ambientTemperature = 100;
        float biomeTemperature = world.getBiome(blockPos).value().getTemperature();
        ambientTemperature += (-1 + biomeTemperature) * 4;
        ambientTemperature -= (blockPos.getY() - world.getSeaLevel()) * 0.01F;
        if (world.getBlockState(blockPos).getFluidState().isIn(FluidTags.WATER)) {
            if (biomeTemperature >= 0)
                ambientTemperature -= 3.3F;
            else ambientTemperature -= 1.3F;
        }
        if (world.isSkyVisible(blockPos)) {
            if (world.isRaining() && world.getBiome(blockPos).value().getPrecipitation(blockPos, world.getSeaLevel()) != Biome.Precipitation.NONE) {
                if (world.getBiome(blockPos).value().getPrecipitation(blockPos, world.getSeaLevel()) == Biome.Precipitation.SNOW)
                    ambientTemperature -= 4F;
                else
                    ambientTemperature -= 2.3F;
            }
            else {
                float maxTimeBonus = 1.5F;
                if (world.getBiome(blockPos).isIn(ConventionalBiomeTags.IS_DRY)) {
                    maxTimeBonus += 2F;
                    if (!world.isDay())
                        ambientTemperature *= 0.9F;
                }
                ambientTemperature += (float) ((maxTimeBonus / 2) * Math.cos(world.getSkyAngleRadians(1.0F)) +1);
            }
        }
        return ambientTemperature;
    }

    public static double getTargetTemperature(LivingEntity entity) {
        double bodyTemperature = entity.getAttributeValue(ThermiaAttributes.BODY_TEMPERATURE);
        return (bodyTemperature + getAmbientTemperature(entity.getWorld(), entity.getBlockPos())) / 2;
    }
}
