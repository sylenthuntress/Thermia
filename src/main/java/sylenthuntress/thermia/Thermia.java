package sylenthuntress.thermia;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sylenthuntress.thermia.registry.*;
import sylenthuntress.thermia.registry.commands.TemperatureCommand;

public class Thermia implements ModInitializer {
    public static final String MOD_ID = "thermia";
    public static final String MOD_NAME = "Thermia";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);

    public static Identifier modIdentifier(String path) {
        return Identifier.of(MOD_ID, path);
    }

    public void onInitialize() {
        Thermia.LOGGER.info(MOD_NAME + " successfully loaded!");
        ThermiaAttributes.registerAll();
        ThermiaStatusEffects.registerAll();
        ThermiaComponents.registerAll();
        ThermiaAttachmentTypes.init();
        ThermiaPotions.registerAll();
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                TemperatureCommand.register(dispatcher.getRoot())
        );
    }
}