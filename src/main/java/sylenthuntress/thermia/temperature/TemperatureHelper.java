package sylenthuntress.thermia.temperature;

import io.wispforest.owo.config.ConfigSynchronizer;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalBiomeTags;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.dimension.DimensionType;
import sylenthuntress.thermia.Thermia;
import sylenthuntress.thermia.access.LivingEntityAccess;
import sylenthuntress.thermia.compat.SereneSeasonsCompatBase;
import sylenthuntress.thermia.data.ThermiaTags;
import sylenthuntress.thermia.registry.ThermiaComponents;
import sylenthuntress.thermia.registry.data_components.SunBlockingComponent;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.ServiceLoader;

@SuppressWarnings("deprecation")
public abstract class TemperatureHelper {
    public static final DecimalFormat DECIMAL_FORMAT = Util.make(
            new DecimalFormat("#.###"), format -> format.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(Locale.ROOT))
    );

    public static double getRegionalTemperature(World world, BlockPos blockPos) {
        double regionalTemperature = 100.0F;

        // Guard-return if config toggle is off
        if (!Thermia.CONFIG.temperatureChecks.DO_REGIONAL()) {
            return regionalTemperature;
        }

        final DimensionType dimension = world.getDimension();
        final RegistryEntry<Biome> biome = world.getBiome(blockPos);
        float biomeTemperature = biome.value().getTemperature();

        if (dimension.ultrawarm())
            biomeTemperature *= 2;

        // Guard return for skylight calculations in nether-like dimensions
        if (dimension.hasCeiling() || dimension.hasFixedTime())
            return (-1 + biomeTemperature) * 5;

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
        if (biomeTemperature >= 0) {
            biomeTemperature *= timeBonus;
        }
        else {
            biomeTemperature = -(-biomeTemperature * timeBonus);
        }

        regionalTemperature += (-1 + biomeTemperature) * 5;
        regionalTemperature -= (blockPos.getY() - world.getSeaLevel()) * 0.13F;
        return regionalTemperature;
    }

    public static double getBlockTemperature(World world, BlockPos blockPos) {
        double blockTemperature = 0;

        // Guard-return if config toggle is off
        if (!Thermia.CONFIG.temperatureChecks.DO_BLOCK()) {
            return blockTemperature;
        }

        final BlockState blockState = world.getBlockState(blockPos);
        if (blockState.get(Properties.WATERLOGGED, false) || blockState.isLiquid()) {
            blockTemperature = getFluidTemperature(world, blockPos);
        }

        blockTemperature += world.getLightLevel(LightType.BLOCK, blockPos) / 4F;

        // Early guard-return
        if (!blockState.isAir())
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
        }

        return blockTemperature;
    }

    public static double getFluidTemperature(World world, BlockPos blockPos) {
        double fluidTemperature = 0;

        // Guard-return if config toggle is off
        if (!Thermia.CONFIG.temperatureChecks.DO_FLUID()) {
            return fluidTemperature;
        }

        for (BlockPos pos : BlockPos.iterate(blockPos.add(-1, -2, -1), blockPos.add(1, 2, 1))) {
            final FluidState fluidState = world.getFluidState(pos);
            if (fluidState.isIn(FluidTags.LAVA)) {
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

    @SuppressWarnings("DuplicateBranchesInSwitch")
    public static double getSeasonalTemperature(World world) {
        double seasonTemperature = 0.0;

        // Guard-return if config toggle is off
        if (!FabricLoader.getInstance().isModLoaded("sereneseasons") || Thermia.CONFIG.temperatureChecks.DO_SEASONAL()) {
            return seasonTemperature;
        }

        ServiceLoader<SereneSeasonsCompatBase> loader = ServiceLoader.load(SereneSeasonsCompatBase.class);
        if (loader.findFirst().isEmpty()) {
            return 0;
        }
        var seasonState = loader.findFirst().get().getSeasonState(world);
        var season = seasonState.getSubSeason();

        switch (season) {
            case EARLY_AUTUMN -> seasonTemperature = 0.5;
            case MID_AUTUMN -> seasonTemperature = 0;
            case LATE_AUTUMN -> seasonTemperature = -0.5;
            case EARLY_WINTER -> seasonTemperature = -1.25;
            case MID_WINTER -> seasonTemperature = -2;
            case LATE_WINTER -> seasonTemperature = -1.25;
            case EARLY_SPRING -> seasonTemperature = -0.5;
            case MID_SPRING -> seasonTemperature = 0;
            case LATE_SPRING -> seasonTemperature = 0.5;
            case EARLY_SUMMER -> seasonTemperature = 1.25;
            case MID_SUMMER -> seasonTemperature = 2;
            case LATE_SUMMER -> seasonTemperature = 1.25;
        }

        return seasonTemperature;
    }

    public static double getAmbientTemperature(World world, BlockPos blockPos) {
        double regionalTemperature = getRegionalTemperature(world, blockPos);
        double blockTemperature = getBlockTemperature(world, blockPos);
        double seasonalTemperature = getSeasonalTemperature(world);

        return regionalTemperature + blockTemperature + seasonalTemperature;
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
    public enum TemperatureScaleDisplay {
        FAHRENHEIT, CELSIUS, KELVIN;

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

        public static Text convertForClient(ServerPlayerEntity player, double temperature) {
            @SuppressWarnings("DataFlowIssue") var temperatureScaleDisplay = (TemperatureScaleDisplay)
                    ConfigSynchronizer.getClientOptions(
                            player,
                            "thermia-config"
                    ).get(Thermia.CONFIG.keys.temperatureScaleDisplay);

            String temperatureScale = "temperature.scale.fahrenheit";
            switch (temperatureScaleDisplay) {
                case CELSIUS -> {
                    temperature = fahrenheitToCelsius(temperature);
                    temperatureScale = "temperature.scale.celsius";
                }
                case KELVIN -> {
                    temperature = fahrenheitToKelvin(temperature);
                    temperatureScale = "temperature.scale.kelvin";
                }
            }

            return Text.literal(
                    DECIMAL_FORMAT.format(temperature)
            ).append(Text.translatable(temperatureScale));
        }
    }
}
