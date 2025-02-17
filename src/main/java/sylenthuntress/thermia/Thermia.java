package sylenthuntress.thermia;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sylenthuntress.thermia.compat.SereneSeasonsCompatBase;
import sylenthuntress.thermia.config.ThermiaConfig;
import sylenthuntress.thermia.event.BaseTemperatureAttributes;
import sylenthuntress.thermia.registry.*;
import sylenthuntress.thermia.registry.command.TemperatureCommand;
import sylenthuntress.thermia.registry.loot_conditions.ThermiaLootConditionTypes;

import java.util.ServiceLoader;

public class Thermia implements ModInitializer {
    public static final String MOD_ID = "thermia";
    public static final String MOD_NAME = "Thermia";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);
    public static boolean SERVER_LOADED = false;
    public static final ThermiaConfig CONFIG = ThermiaConfig.createAndLoad();

    public static Identifier modIdentifier(String path) {
        return Identifier.of(MOD_ID, path);
    }

    public void onInitialize() {
        Thermia.LOGGER.info(MOD_NAME + " by SylentHuntress successfully loaded!");
        ServerLifecycleEvents.SERVER_STARTED.register(minecraftServer -> SERVER_LOADED = true);

        ThermiaAttributes.registerAll();
        ThermiaStatusEffects.registerAll();
        ThermiaComponents.registerAll();
        ThermiaAttachmentTypes.registerAll();
        ThermiaPotions.registerAll();
        ThermiaCriteria.registerAll();
        ThermiaLootConditionTypes.registerAll();
        ThermiaItems.registerAll();

        ServerEntityEvents.ENTITY_LOAD.register(new BaseTemperatureAttributes());

        if (FabricLoader.getInstance().isModLoaded("serene-seasons")) {
            ServiceLoader<SereneSeasonsCompatBase> loader = ServiceLoader.load(SereneSeasonsCompatBase.class);
            if (loader.findFirst().isPresent()) {
                loader.findFirst().get();
            }
        }

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                TemperatureCommand.register(dispatcher.getRoot())
        );

        // Apply thermoregulation on respawn
        ServerPlayerEvents.AFTER_RESPAWN.register((
                oldPlayer,
                newPlayer,
                alive) -> newPlayer.addStatusEffect(
                new StatusEffectInstance(
                        ThermiaStatusEffects.THERMOREGULATION,
                        1500,
                        0,
                        false,
                        false,
                        true
                ),
                null
        ));
    }
}