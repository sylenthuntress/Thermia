package sylenthuntress.thermia.temperature;

import net.fabricmc.fabric.api.tag.convention.v2.ConventionalBiomeTags;
import net.minecraft.entity.LivingEntity;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import sylenthuntress.thermia.access.temperature.LivingEntityAccess;

public abstract class TemperatureHelper {
    public static double getBiomeTemperature(World world, BlockPos blockPos) {
        float biomeTemperature = world.getBiome(blockPos).value().getTemperature();
        if (world.getDimension().ultrawarm())
            biomeTemperature *= 2;

        if (!world.getDimension().hasCeiling() && !world.getDimension().hasFixedTime()) {
            boolean dryBiome = world.getBiome(blockPos).isIn(ConventionalBiomeTags.IS_DRY);
            float maxTimeBonus = dryBiome ? 2.5F : 1F;
            float timeBonus = (float) ((maxTimeBonus / 2) * Math.cos(world.getSkyAngleRadians(1.0F)) + 1);
            if (!world.isSkyVisible(blockPos.add(0, 1, 0)))
                timeBonus -= maxTimeBonus * 0.5F;
            if (biomeTemperature >= 0)
                biomeTemperature *= timeBonus;
            else {
                biomeTemperature = -(-biomeTemperature * timeBonus);
            }
        }
        biomeTemperature = (-1 + biomeTemperature) * 5;
        return biomeTemperature;
    }

    public static double getAmbientTemperature(World world, BlockPos blockPos) {
        double ambientTemperature = 100;
        double biomeTemperature = getBiomeTemperature(world, blockPos);
        ambientTemperature += biomeTemperature;
        ambientTemperature -= (blockPos.getY() - world.getSeaLevel()) * 0.25F;
        ambientTemperature += world.getLightLevel(LightType.BLOCK, blockPos) / 4F;
        if (world.getBlockState(blockPos).getFluidState().isIn(FluidTags.WATER)) {
            if (biomeTemperature >= 0)
                ambientTemperature -= 3.3F;
            else ambientTemperature -= 1.3F;
        }
        return ambientTemperature;
    }

    public static TemperatureManager getTemperatureManager(LivingEntity entity) {
        return ((LivingEntityAccess) entity).thermia$getTemperatureManager();
    }
}
