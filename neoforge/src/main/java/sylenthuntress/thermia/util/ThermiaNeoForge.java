package sylenthuntress.thermia.util;


import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import sylenthuntress.thermia.Thermia;

@Mod(Constants.MOD_ID)
public class ThermiaNeoForge {

    public ThermiaNeoForge(IEventBus eventBus) {

        // This method is invoked by the NeoForge mod loader when it is ready
        // to load your mod. You can access NeoForge and Common code in this
        // project.

        // Use NeoForge to bootstrap the Common mod.
        Constants.LOGGER.info("Hello NeoForge world!");
        Thermia.init();

    }
}