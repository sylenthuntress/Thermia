package sylenthuntress.thermia.util;

import net.fabricmc.fabric.api.tag.convention.v2.ConventionalBiomeTags;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import sylenthuntress.thermia.registry.ThermiaAttributes;

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
        if (world.getBlockState(blockPos).getFluidState().isIn(FluidTags.WATER)) {
            if (biomeTemperature >= 0)
                ambientTemperature -= 3.3F;
            else ambientTemperature -= 1.3F;
        }
        return ambientTemperature;
    }

    public static double[] getExtremeTemperatures(LivingEntity entity) {
        double coldTemperature = 0;
        double hotTemperature = 0;

        if (entity.isOnFire() && !entity.isFireImmune())
            hotTemperature += entity.getFireTicks() * 0.1;
        else if (entity.canFreeze())
            coldTemperature += entity.getFrozenTicks() * 0.1;

        DamageSource recentDamageSource = entity.getRecentDamageSource();
        if (recentDamageSource != null) {
            float lastDamageTaken = entity.lastDamageTaken;
            if (recentDamageSource.isIn(DamageTypeTags.IS_FIRE))
                hotTemperature += lastDamageTaken;
            else if (recentDamageSource.isIn(DamageTypeTags.IS_FREEZING))
                coldTemperature += lastDamageTaken;
        }

        return new double[]{
                -coldTemperature,
                hotTemperature
        };
    }

    public static double getTargetTemperature(LivingEntity entity) {
        double bodyTemperature = entity.getAttributeValue(ThermiaAttributes.BODY_TEMPERATURE);
        double ambientTemperature = getAmbientTemperature(entity.getWorld(), entity.getBlockPos());

        return (bodyTemperature + ambientTemperature) / 2;
    }
}
