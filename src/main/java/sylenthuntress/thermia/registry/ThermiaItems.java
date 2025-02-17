package sylenthuntress.thermia.registry;

import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import sylenthuntress.thermia.Thermia;

public class ThermiaItems {
    public static final Item THERMIA_ICON = register(
            Thermia.modIdentifier("icon")
    );

    protected static Item register(Identifier id) {
        return Registry.register(
                Registries.ITEM,
                id,
                new Item(new Item.Settings().registryKey(RegistryKey.of(RegistryKeys.ITEM, id)))
        );
    }

    public static void registerAll() {

    }
}
