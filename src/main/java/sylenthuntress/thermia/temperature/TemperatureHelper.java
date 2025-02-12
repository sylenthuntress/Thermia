package sylenthuntress.thermia.temperature;

import net.fabricmc.fabric.api.tag.convention.v2.ConventionalBiomeTags;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.dimension.DimensionType;
import sylenthuntress.thermia.access.temperature.LivingEntityAccess;
import sylenthuntress.thermia.registry.ThermiaComponents;
import sylenthuntress.thermia.registry.ThermiaTags;
import sylenthuntress.thermia.registry.data_components.SunBlockingComponent;

@SuppressWarnings("deprecation")
public abstract class TemperatureHelper {
    public static double getRegionalTemperature(World world, BlockPos blockPos) {
        final DimensionType dimension = world.getDimension();
        final RegistryEntry<Biome> biome = world.getBiome(blockPos);
        float regionalTemperature = biome.value().getTemperature();

        if (dimension.ultrawarm())
            regionalTemperature *= 2;

        // Guard return for skylight calculations in nether-like dimensions
        if (dimension.hasCeiling() || dimension.hasFixedTime())
            return (-1 + regionalTemperature) * 5;

        // Calculate skylight modifier
        float maxTimeBonus = biome.isIn(ConventionalBiomeTags.IS_DRY) ? 2.5F : 1F;
        float timeBonus = (float) (
                (maxTimeBonus / 2) * Math.cos(
                        world.getSkyAngleRadians(1.0F)
                ) + 1
        );

        if (!world.isSkyVisibleAllowingSea(blockPos.add(0, 1, 0))) {
            timeBonus -= maxTimeBonus * 0.5F;
        }

        for (Entity entity
                : world.getNonSpectatingEntities(Entity.class, Box.from(Vec3d.of(blockPos.add(0, 6, 0))))) {
            timeBonus -= 0.1F;

            if (entity instanceof LivingEntity livingEntity) {
                for (EquipmentSlot slot : EquipmentSlot.VALUES) {
                    final var component = livingEntity.getEquippedStack(slot).getOrDefault(
                            ThermiaComponents.SUN_BLOCKING,
                            SunBlockingComponent.DEFAULT
                    );

                    if (!component.slot().matches(slot)) {
                        continue;
                    }

                    timeBonus -= component.amount();
                }
            }
        }

        // Apply skylight modifier
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

        blockTemperature -= (blockPos.getY() - world.getSeaLevel()) * 0.1F;
        blockTemperature += world.getLightLevel(LightType.BLOCK, blockPos) / 4f;

        // Early guard-return
        if (!world.getBlockState(blockPos).isAir())
            return blockTemperature;

        for (BlockPos pos : BlockPos.iterate(blockPos.add(-4, -4, -4), blockPos.add(4, 4, 4))) {
            blockTemperature += world.getReceivedRedstonePower(pos) / 8F;

            final BlockState nearbyBlock = world.getBlockState(pos);
            if (nearbyBlock.isIn(ThermiaTags.Block.COLD_BLOCKS)) {
                blockTemperature -= 0.2;
            }
            if (nearbyBlock.isIn(ThermiaTags.Block.HOT_BLOCKS)
                    || nearbyBlock.get(Properties.LIT, false)) {
                blockTemperature += 0.2;
            }

            final FluidState fluidState = nearbyBlock.getFluidState();
            if (fluidState.isIn(FluidTags.LAVA) && fluidState.isStill()) {
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
            }

            if (world.getBlockState(pos).get(Properties.WATERLOGGED, false)
                    || fluidState.isIn(FluidTags.WATER)) {
                if (getRegionalTemperature(world, pos) < 0) {
                    fluidTemperature -= 0.1f;
                }
                fluidTemperature -= 0.1f;
            }
        }

        return fluidTemperature;
    }

    public static double getAmbientTemperature(World world, BlockPos blockPos) {
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
                && getTemperatureManager(entity).canHaveTemperature());
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
