package sylenthuntress.thermia.config;

import io.wispforest.owo.config.Option;
import io.wispforest.owo.config.annotation.Config;
import io.wispforest.owo.config.annotation.Modmenu;
import io.wispforest.owo.config.annotation.Sync;
import sylenthuntress.thermia.Thermia;

@SuppressWarnings("unused")
@Sync(value = Option.SyncMode.OVERRIDE_CLIENT)
@Modmenu(modId = Thermia.MOD_ID)
@Config(name = "thermia-config", wrapperName = "ThermiaConfig")
public class ThermiaConfigModel {
    // Client-only settings
    @Sync(value = Option.SyncMode.INFORM_SERVER)
    public TemperatureScaleDisplay temperatureScaleDisplay = TemperatureScaleDisplay.FAHRENHEIT;

    public enum TemperatureScaleDisplay {
        FAHRENHEIT, CELSIUS, KELVIN
    }
}
