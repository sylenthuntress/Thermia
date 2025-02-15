package sylenthuntress.thermia.event;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalBiomeTags;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.biome.Biome;
import sylenthuntress.thermia.registry.ThermiaAttributes;
import sylenthuntress.thermia.temperature.TemperatureHelper;

public class BaseTemperatureAttributes implements ServerEntityEvents.Load {
    @Override
    public void onLoad(Entity entity, ServerWorld world) {
        if (!(entity instanceof LivingEntity livingEntity) || entity.isPlayer()) {
            return;
        }

        calculateBaseTemperature(livingEntity, world);
        calculateOffsets(livingEntity, world);
    }

    protected void calculateBaseTemperature(LivingEntity entity, ServerWorld world) {
        final var temperatureManager = TemperatureHelper.getTemperatureManager(entity);
        double baseTemperature = temperatureManager.getBaseTemperature();

        // Guard-return if following calculations have already been done
        if (baseTemperature != ThermiaAttributes.BASE_TEMPERATURE.value().getDefaultValue()) {
            return;
        }

        double ambientTemperature = TemperatureHelper.getAmbientTemperature(world, entity.getBlockPos());
        baseTemperature = (baseTemperature + ambientTemperature) / 2;

        var attributeInstance = entity.getAttributes().getCustomInstance(ThermiaAttributes.BASE_TEMPERATURE);
        if (attributeInstance != null) {
            attributeInstance.setBaseValue(baseTemperature);
        }
    }

    protected void calculateOffsets(LivingEntity entity, ServerWorld world) {
        double coldOffset = entity.getAttributeValue(ThermiaAttributes.COLD_OFFSET_THRESHOLD);
        double heatOffset = entity.getAttributeValue(ThermiaAttributes.HEAT_OFFSET_THRESHOLD);

        // Guard-return if following calculations have already been done
        if (coldOffset != ThermiaAttributes.COLD_OFFSET_THRESHOLD.value().getDefaultValue()) {
            return;
        } else if (heatOffset != ThermiaAttributes.HEAT_OFFSET_THRESHOLD.value().getDefaultValue()) {
            return;
        }

        var biome = world.getBiome(entity.getBlockPos());

        if (biome.isIn(ConventionalBiomeTags.IS_DRY)) {
            heatOffset += 3;
            coldOffset += 3;
        }
        if (biome.value().getPrecipitation(entity.getBlockPos(), world.getSeaLevel()) == Biome.Precipitation.SNOW) {
            coldOffset += 3;
        }

        var attributeInstance = entity.getAttributes().getCustomInstance(ThermiaAttributes.COLD_OFFSET_THRESHOLD);
        if (attributeInstance != null) {
            attributeInstance.setBaseValue(coldOffset);
        }

        attributeInstance = entity.getAttributes().getCustomInstance(ThermiaAttributes.HEAT_OFFSET_THRESHOLD);
        if (attributeInstance != null) {
            attributeInstance.setBaseValue(heatOffset);
        }
    }
}
