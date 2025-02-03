package sylenthuntress.thermia.temperature;

import net.fabricmc.fabric.api.tag.convention.v2.ConventionalBiomeTags;
import net.minecraft.entity.LivingEntity;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import sylenthuntress.thermia.Thermia;
import sylenthuntress.thermia.access.temperature.LivingEntityAccess;
import sylenthuntress.thermia.registry.ThermiaAttributes;

import java.util.ArrayList;
import java.util.List;

public abstract class TemperatureHelper {
    public static double getBiomeTemperature(World world, BlockPos blockPos) {
        float biomeTemperature = world.getBiome(blockPos).value().getTemperature();

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
        biomeTemperature = (-1 + biomeTemperature) * 5;
        return biomeTemperature;
    }

    public static double getAmbientTemperature(World world, BlockPos blockPos) {
        double ambientTemperature = 100;
        double biomeTemperature = getBiomeTemperature(world, blockPos);
        ambientTemperature += biomeTemperature;
        ambientTemperature -= (blockPos.getY() - world.getSeaLevel()) * 0.1F;
        ambientTemperature += world.getLightLevel(LightType.BLOCK, blockPos) / 4F;
        if (world.getBlockState(blockPos).getFluidState().isIn(FluidTags.WATER)) {
            if (biomeTemperature >= 0)
                ambientTemperature -= 3.3F;
            else ambientTemperature -= 1.3F;
        }
        return ambientTemperature;
    }

    public static double getTargetTemperature(LivingEntity entity) {
        double bodyTemperature = entity.getAttributeValue(ThermiaAttributes.BODY_TEMPERATURE);
        double ambientTemperature = getAmbientTemperature(entity.getWorld(), entity.getBlockPos());

        return (bodyTemperature + ambientTemperature) / 2;
    }

    public static boolean removeModifier(LivingEntity entity, Identifier id) {
        TemperatureManager temperatureManager = ((LivingEntityAccess)entity).thermia$getTemperatureManager();
        List<TemperatureModifier> temperatureModifiers = temperatureManager.getTemperatureModifiers();

        int index = 0;
        for (TemperatureModifier modifier : temperatureModifiers) {
            if (modifier.idMatches(id)) {
                temperatureModifiers.remove(index);
                return true;
            }
            index++;
        }
        return false;
    }

    public static boolean removeModifiers(LivingEntity entity, Identifier... modifiers) {
        boolean bl = false;
        for (Identifier modifier : modifiers) {
            removeModifier(entity, modifier);
            bl = true;
        }
        return bl;
    }

    public static boolean removeThermiaModifiers(LivingEntity entity, String... modifiers) {
        boolean bl = false;
        for (String modifierName : modifiers) {
            removeModifier(entity, Thermia.modIdentifier(modifierName));
            bl = true;
        }
        return bl;
    }

    public static boolean addModifier(LivingEntity entity, TemperatureModifier modifier) {
        if (!hasModifier(entity, modifier.id())) {
            TemperatureManager temperatureManager = ((LivingEntityAccess)entity).thermia$getTemperatureManager();
            temperatureManager.getTemperatureModifiers().add(modifier);
            return true;
        }
        return false;
    }

    public static boolean addModifiers(LivingEntity entity, TemperatureModifier... modifiers) {
        boolean bl = false;
        for (TemperatureModifier modifier : modifiers) {
            addModifier(entity, modifier);
            bl = true;
        }
        return bl;
    }

    public static boolean hasModifier(LivingEntity entity, Identifier id) {
        List<TemperatureModifier> temperatureModifiers = ((LivingEntityAccess)entity).thermia$getTemperatureManager().getTemperatureModifiers();
        return temperatureModifiers.stream().anyMatch((modifier -> modifier.idMatches(id)));
    }

    public static TemperatureModifier getModifier(LivingEntity entity, Identifier id) {
        List<TemperatureModifier> temperatureModifiers = ((LivingEntityAccess)entity).thermia$getTemperatureManager().getTemperatureModifiers();
        for (TemperatureModifier modifier : temperatureModifiers) {
            if (modifier.idMatches(id))
                return modifier;
        } return null;
    }
}
