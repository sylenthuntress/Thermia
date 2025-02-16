package sylenthuntress.thermia.config;

import blue.endless.jankson.Comment;
import io.wispforest.owo.config.Option;
import io.wispforest.owo.config.annotation.*;
import sylenthuntress.thermia.Thermia;
import sylenthuntress.thermia.temperature.TemperatureHelper.TemperatureScaleDisplay;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
@Sync(value = Option.SyncMode.OVERRIDE_CLIENT)
@Modmenu(modId = Thermia.MOD_ID)
@Config(name = "thermia-config", wrapperName = "ThermiaConfig")
public class ThermiaConfigModel {
    // Client-only settings
    @SectionHeader(value = "clientSection")
    @Sync(value = Option.SyncMode.INFORM_SERVER)
    @Comment(ThermiaConfigComments.TEMPERATURE_SCALE_DISPLAY)
    public TemperatureScaleDisplay temperatureScaleDisplay = TemperatureScaleDisplay.FAHRENHEIT;

    @Nest
    @Expanded
    @Sync(value = Option.SyncMode.NONE)
    @Comment(ThermiaConfigComments.CLIMATE_EFFECT_DISPLAY)
    public ClimateEffectDisplay climateEffectDisplay = new ClimateEffectDisplay();

    // Temperature settings
    @SectionHeader(value = "temperatureSection")
    @Nest
    @Expanded
    @Comment(ThermiaConfigComments.TEMPERATURE_CHECKS)
    public TemperatureChecks temperatureChecks = new TemperatureChecks();
    @Nest
    public EntityTemperature entityTemperature = new EntityTemperature();

    public static class ClimateEffectDisplay {
        @Sync(value = Option.SyncMode.INFORM_SERVER)
        public boolean SHOW_HYPOTHERMIA = true;
        public boolean CUSTOM_HYPOTHERMIA_DISPLAY = true;

        @Sync(value = Option.SyncMode.INFORM_SERVER)
        public boolean SHOW_HYPERTHERMIA = true;
        public boolean CUSTOM_HYPERTHERMIA_DISPLAY = true;
    }

    public static class TemperatureChecks {
        public boolean DO_BLOCK = true;
        public boolean DO_FLUID = true;
        public boolean DO_REGIONAL = true;
        public boolean DO_SEASONAL = true;
    }

    public static class EntityTemperature {
        public boolean CAN_FREEZE = true;
        public boolean CAN_OVERHEAT = true;

        @Nest
        public EntityTags entityTags = new EntityTags();

        public static class EntityTags {
            @Expanded
            public List<String> UNAFFECTED_LIST = new ArrayList<>(
                    List.of(
                            "#thermia:climate/unaffected",
                            "#thermia:climate/temperature_immune"
                    )
            );
            public boolean UNAFFECTED_LIST_INVERT = false;

            @Expanded
            public List<String> HAS_FUR_LIST = new ArrayList<>(
                    List.of(
                            "#thermia:climate/has_fur"
                    )
            );
            public boolean HAS_FUR_LIST_INVERT = false;

            @Expanded
            public List<String> HAS_WOOL_LIST = new ArrayList<>(
                    List.of(
                            "#thermia:climate/has_wool"
                    )
            );
            public boolean HAS_WOOL_LIST_INVERT = false;

            @Expanded
            public List<String> UNDEAD_LIST = new ArrayList<>(
                    List.of(
                            "#thermia:climate/is_undead"
                    )
            );
            public boolean UNDEAD_LIST_INVERT = false;

            @Expanded
            public List<String> TEMPERATURE_IMMUNE_LIST = new ArrayList<>(
                    List.of(
                            "#thermia:climate/temperature_immune"
                    )
            );
            public boolean TEMPERATURE_IMMUNE_LIST_INVERT = false;
        }
    }
}
