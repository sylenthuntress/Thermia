package sylenthuntress.thermia.temperature;

import net.fabricmc.fabric.api.tag.convention.v2.ConventionalBiomeTags;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.dimension.DimensionType;
import org.jetbrains.annotations.Nullable;
import sylenthuntress.thermia.access.temperature.LivingEntityAccess;
import sylenthuntress.thermia.registry.ThermiaTags;

public abstract class TemperatureHelper {
    public static double getRegionalTemperature(World world, BlockPos blockPos) {
        final DimensionType dimension = world.getDimension();
        final RegistryEntry<Biome> biome = world.getBiome(blockPos);
        float regionalTemperature = biome.value().getTemperature();

        if (dimension.ultrawarm())
            regionalTemperature *= 2;

        // Guard return in nether-like dimensions
        if (dimension.hasCeiling() || dimension.hasFixedTime())
            return (-1 + regionalTemperature) * 5;

        float maxTimeBonus = biome.isIn(ConventionalBiomeTags.IS_DRY) ? 2.5F : 1F;
        float timeBonus = (float) (
                (maxTimeBonus / 2) * Math.cos(
                        world.getSkyAngleRadians(1.0F)
                ) + 1
        );

        if (!world.isSkyVisibleAllowingSea(blockPos.add(0, 1, 0))) {
            timeBonus -= maxTimeBonus * 0.5F;
        }
        if (regionalTemperature >= 0) {
            regionalTemperature *= timeBonus;
        }
        else {
            regionalTemperature = -(-regionalTemperature * timeBonus);
        }

        return (-1 + regionalTemperature) * 5;
    }

    public static double getBlockTemperature(World world, BlockPos blockPos) {
        double blockTemperature = 0;
        if (world.getBlockState(blockPos).get(Properties.WATERLOGGED, false) || world.getBlockState(blockPos).isLiquid())
            blockTemperature = getFluidTemperature(world, blockPos);

        blockTemperature -= (blockPos.getY() - world.getSeaLevel()) * 0.13F;
        blockTemperature += world.getLightLevel(LightType.BLOCK, blockPos) / 4f;

        // Early guard-return
        if (!world.getBlockState(blockPos).isAir())
            return blockTemperature;

        for (BlockPos pos : BlockPos.iterate(blockPos.add(-4, -4, -4), blockPos.add(4, 4, 4))) {
            BlockState blockState = world.getBlockState(pos);
            if (blockState.isIn(ThermiaTags.Block.COLD_BLOCKS))
                blockTemperature -= 0.1;
            if (blockState.isIn(ThermiaTags.Block.HOT_BLOCKS))
                blockTemperature += 0.1;

            FluidState fluidState = blockState.getFluidState();
            if (fluidState.isIn(FluidTags.LAVA) && fluidState.isStill())
                blockTemperature += 1;
        }

        return blockTemperature;
    }

    public static double getFluidTemperature(World world, BlockPos blockPos) {
        double fluidTemperature = 0;

        for (BlockPos pos : BlockPos.iterate(blockPos.add(-1, -2, -1), blockPos.add(1, 2, 1))) {
            FluidState fluidState = world.getFluidState(pos);
            if (fluidState.isIn(FluidTags.LAVA))
                fluidTemperature += 1f;
            if (world.getBlockState(pos).get(Properties.WATERLOGGED, false) || fluidState.isIn(FluidTags.WATER)) {
                if (getRegionalTemperature(world, pos) > 0)
                    fluidTemperature -= 0.1f;
                fluidTemperature -= 0.025f;
            }
        }

        return fluidTemperature;
    }

    public static double getAmbientTemperature(World world, BlockPos blockPos, @Nullable Entity entity) {
        double ambientTemperature = 100;
        double biomeTemperature = getRegionalTemperature(world, blockPos);
        double blockTemperature = getBlockTemperature(world, blockPos);
        return ambientTemperature + biomeTemperature + blockTemperature;
    }

    public static TemperatureManager getTemperatureManager(LivingEntity entity) {
        return ((LivingEntityAccess) entity).thermia$getTemperatureManager();
    }

    public static TemperatureManager getTemperatureManager(Entity entity) {
        return getTemperatureManager((LivingEntity) entity);
    }

    public static boolean lacksTemperature(Entity entity) {
        return !(entity.isLiving()
                && getTemperatureManager(entity).hasTemperature());
    }

    @SuppressWarnings("unused")
    public static class Conversions {
        public static double celsiusToFahrenheit(double temperature) {
            return (temperature * 9 / 5) + 32;
        }

        public static double celsiusToKelvin(double temperature) {
            return temperature + 273.15;
        }

        public static double fahrenheitToCelsius(double temperature) {
            return (temperature - 32) * 5 / 9;
        }

        public static double fahrenheitToKelvin(double temperature) {
            return (temperature - 32) * 5 / 9 + 273.15;
        }

        public static double kelvinToFahrenheit(double temperature) {
            return (temperature - 273.15) * 9 / 5 + 32;
        }

        public static double kelvinToCelsius(double temperature) {
            return temperature - 273.15;
        }
    }
}
