package sylenthuntress.thermia.registry;

import net.minecraft.block.Block;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import sylenthuntress.thermia.Thermia;

public class ThermiaTags {
    public static class Block {
        public static final TagKey<net.minecraft.block.Block> COLD_BLOCKS = TagKey.of(
                RegistryKeys.BLOCK,
                Thermia.modIdentifier("climate/cold_blocks")
        );
        public static final TagKey<net.minecraft.block.Block> HOT_BLOCKS = TagKey.of(
                RegistryKeys.BLOCK,
                Thermia.modIdentifier("climate/hot_blocks")
        );
    }
    public static class EntityType {
        public static final TagKey<net.minecraft.entity.EntityType<?>> COLD_MOBS = TagKey.of(
                RegistryKeys.ENTITY_TYPE,
                Thermia.modIdentifier("climate/cold_mobs")
        );
        public static final TagKey<net.minecraft.entity.EntityType<?>> HOT_MOBS = TagKey.of(
                RegistryKeys.ENTITY_TYPE,
                Thermia.modIdentifier("climate/hot_mobs")
        );
        public static final TagKey<net.minecraft.entity.EntityType<?>> NETHER_MOBS = TagKey.of(
                RegistryKeys.ENTITY_TYPE,
                Thermia.modIdentifier("climate/nether_mobs")
        );
        public static final TagKey<net.minecraft.entity.EntityType<?>> UNDEAD_MOBS = TagKey.of(
                RegistryKeys.ENTITY_TYPE,
                Thermia.modIdentifier("climate/undead_mobs")
        );
        public static final TagKey<net.minecraft.entity.EntityType<?>> TEMPERATURE_IMMUNE = TagKey.of(
                RegistryKeys.ENTITY_TYPE,
                Thermia.modIdentifier("climate/temperature_immune")
        );
        public static final TagKey<net.minecraft.entity.EntityType<?>> CLIMATE_AFFECTED = TagKey.of(
                RegistryKeys.ENTITY_TYPE,
                Thermia.modIdentifier("climate/affected")
        );
    }
    public static class Item {
        public static class Consumable {
            public static final TagKey<net.minecraft.item.Item> COLD_FOODS = TagKey.of(
                    RegistryKeys.ITEM,
                    Thermia.modIdentifier("consumable_temperature/cold_foods")
            );
            public static final TagKey<net.minecraft.item.Item> HOT_FOODS = TagKey.of(
                    RegistryKeys.ITEM,
                    Thermia.modIdentifier("consumable_temperature/hot_foods")
            );
            public static final TagKey<net.minecraft.item.Item> REFRESHING_FOODS = TagKey.of(
                    RegistryKeys.ITEM,
                    Thermia.modIdentifier("consumable_temperature/refreshing_foods")
            );
            public static final TagKey<net.minecraft.item.Item> WARM_FOODS = TagKey.of(
                    RegistryKeys.ITEM,
                    Thermia.modIdentifier("consumable_temperature/warm_foods")
            );

            public static final TagKey<net.minecraft.item.Item> APPLIES_FROST_RESISTANCE = TagKey.of(
                    RegistryKeys.ITEM,
                    Thermia.modIdentifier("applies_frost_resistance")
            );
        }

        public static class Equippable {
            public static final TagKey<net.minecraft.item.Item> INSULATING = TagKey.of(
                    RegistryKeys.ITEM,
                    Thermia.modIdentifier("equippable_temperature/insulating")
            );
        }
    }
}
