package sylenthuntress.thermia.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import sylenthuntress.thermia.client.event.ConsumableTemperatureTooltip;

public class ThermiaClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ItemTooltipCallback.EVENT.register(new ConsumableTemperatureTooltip());
    }
}
