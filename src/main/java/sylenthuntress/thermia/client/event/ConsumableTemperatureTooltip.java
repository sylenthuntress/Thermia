package sylenthuntress.thermia.client.event;

import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import sylenthuntress.thermia.Thermia;
import sylenthuntress.thermia.registry.ThermiaComponents;
import sylenthuntress.thermia.registry.data_component.TemperatureModifiersComponent;

import java.util.List;

public class ConsumableTemperatureTooltip implements ItemTooltipCallback {
    @SuppressWarnings("DataFlowIssue")
    @Override
    public void getTooltip(ItemStack stack, Item.TooltipContext context, TooltipType type, List<Text> lines) {
        if (!stack.contains(ThermiaComponents.CONSUMABLE_TEMPERATURE)) {
            return;
        }

        lines.add(1,
                Text.translatable(
                        "temperature.modifiers.consumable"
                ).formatted(Formatting.GRAY)
        );
        lines.add(1,
                ScreenTexts.EMPTY
        );

        double amount = stack.get(ThermiaComponents.CONSUMABLE_TEMPERATURE).temperature();
        double minAmount = stack.get(ThermiaComponents.CONSUMABLE_TEMPERATURE).minTemperature();
        double maxAmount = stack.get(ThermiaComponents.CONSUMABLE_TEMPERATURE).maxTemperature();
        String temperatureScale = "temperature.scale.fahrenheit";

        switch (Thermia.CONFIG.temperatureScaleDisplay()) {
            case CELSIUS -> {
                amount *= 5.0 / 9.0;
                minAmount *= 5.0 / 9.0;
                maxAmount *= 5.0 / 9.0;
                temperatureScale = "temperature.scale.celsius";
            }
            case KELVIN -> {
                amount *= 5.0 / 9.0;
                minAmount *= 5.0 / 9.0;
                maxAmount *= 5.0 / 9.0;
                temperatureScale = "temperature.scale.kelvin";
            }
        }

        if (minAmount < maxAmount) {
            if (amount != 0) {
                lines.add(3,
                        Text.translatable(
                                "temperature.modifier.random",
                                minAmount,
                                maxAmount
                        ).append(
                                Text.translatable(temperatureScale)
                        ).formatted(Formatting.GRAY, Formatting.ITALIC)
                );
            } else {
                if (minAmount > 0.0) {
                    lines.add(3,
                            Text.translatable(
                                    "temperature.modifier.random.hot",
                                    TemperatureModifiersComponent.DECIMAL_FORMAT.format(minAmount),
                                    TemperatureModifiersComponent.DECIMAL_FORMAT.format(maxAmount)
                            ).append(
                                    Text.translatable(temperatureScale)
                            ).append(
                                    Text.translatable("temperature.symbol.fire", " ")
                            ).formatted(Formatting.GOLD)
                    );
                } else if (minAmount < 0.0 && maxAmount < 0.0) {
                    lines.add(3,
                            Text.translatable(
                                    "temperature.modifier.random.cold",
                                    TemperatureModifiersComponent.DECIMAL_FORMAT.format(minAmount),
                                    TemperatureModifiersComponent.DECIMAL_FORMAT.format(maxAmount)
                            ).append(
                                    Text.translatable(temperatureScale)
                            ).append(
                                    Text.translatable("temperature.symbol.snowflake", " ")
                            ).formatted(Formatting.AQUA)
                    );
                } else {
                    lines.add(3,
                            Text.translatable(
                                    "temperature.modifier.random.neutral",
                                    minAmount,
                                    maxAmount
                            ).append(
                                    Text.translatable(temperatureScale)
                            ).formatted(Formatting.BLUE)
                    );
                }

                return;
            }
        }

        if (amount > 0.0) {
            lines.add(3,
                    Text.translatable(
                            "temperature.modifier.hot.0",
                            TemperatureModifiersComponent.DECIMAL_FORMAT.format(amount)
                    ).append(
                            Text.translatable(temperatureScale)
                    ).append(
                            Text.translatable("temperature.symbol.fire", " ")
                    ).formatted(Formatting.GOLD)
            );
        } else if (amount < 0.0) {
            lines.add(3,
                    Text.translatable(
                            "temperature.modifier.cold.0",
                            TemperatureModifiersComponent.DECIMAL_FORMAT.format(-amount)
                    ).append(
                            Text.translatable(temperatureScale)
                    ).append(
                            Text.translatable("temperature.symbol.snowflake", " ")
                    ).formatted(Formatting.AQUA)
            );
        }
    }
}
