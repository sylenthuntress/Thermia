package sylenthuntress.thermia.registry;

import net.fabricmc.fabric.api.registry.FabricBrewingRecipeRegistryBuilder;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.Items;
import net.minecraft.potion.Potion;
import net.minecraft.potion.Potions;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import sylenthuntress.thermia.Thermia;

public class ThermiaPotions {
    public static final RegistryEntry<Potion> FROST_RESISTANCE = register(
            "frost_resistance",
            new Potion("frost_resistance",
                    new StatusEffectInstance(
                            ThermiaStatusEffects.FROST_RESISTANCE,
                            3600,
                            0
                    )
            )
    );
    public static final RegistryEntry<Potion> LONG_FROST_RESISTANCE = register(
            "long_frost_resistance",
            new Potion("frost_resistance",
                    new StatusEffectInstance(
                            ThermiaStatusEffects.FROST_RESISTANCE,
                            9600
                    )
            )
    );

    private static RegistryEntry<Potion> register(String id, Potion potion) {
        return Registry.registerReference(Registries.POTION, Thermia.modIdentifier(id), potion);
    }

    public static void registerAll() {
        FabricBrewingRecipeRegistryBuilder.BUILD.register(builder -> {
            builder.registerPotionRecipe(
                    Potions.FIRE_RESISTANCE,
                    Items.FERMENTED_SPIDER_EYE,
                    FROST_RESISTANCE
            );

            builder.registerPotionRecipe(
                    FROST_RESISTANCE,
                    Items.REDSTONE,
                    LONG_FROST_RESISTANCE
            );
        });

    }
}
