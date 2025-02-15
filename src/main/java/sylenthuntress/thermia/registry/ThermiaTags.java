package sylenthuntress.thermia.registry;

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
        public static final TagKey<net.minecraft.entity.EntityType<?>> CLIMATE_UNAFFECTED = TagKey.of(
                RegistryKeys.ENTITY_TYPE,
                Thermia.modIdentifier("climate/unaffected")
        );
        public static final TagKey<net.minecraft.entity.EntityType<?>> HAS_FUR = TagKey.of(
                RegistryKeys.ENTITY_TYPE,
                Thermia.modIdentifier("climate/has_fur")
        );
        public static final TagKey<net.minecraft.entity.EntityType<?>> HAS_WOOL = TagKey.of(
                RegistryKeys.ENTITY_TYPE,
                Thermia.modIdentifier("climate/has_wool")
        );
        public static final TagKey<net.minecraft.entity.EntityType<?>> UNDEAD_MOBS = TagKey.of(
                RegistryKeys.ENTITY_TYPE,
                Thermia.modIdentifier("climate/undead_mobs")
        );
        public static final TagKey<net.minecraft.entity.EntityType<?>> TEMPERATURE_IMMUNE = TagKey.of(
                RegistryKeys.ENTITY_TYPE,
                Thermia.modIdentifier("climate/temperature_immune")
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

            public static final TagKey<net.minecraft.item.Item> BREEZY = TagKey.of(
                    RegistryKeys.ITEM,
                    Thermia.modIdentifier("equippable_temperature/breezy")
            );

            public static final TagKey<net.minecraft.item.Item> COLD_WHEN_HELD = TagKey.of(
                    RegistryKeys.ITEM,
                    Thermia.modIdentifier("equippable_temperature/cold_when_held")
            );

            public static final TagKey<net.minecraft.item.Item> HOT_WHEN_HELD = TagKey.of(
                    RegistryKeys.ITEM,
                    Thermia.modIdentifier("equippable_temperature/hot_when_held")
            );

            public static final TagKey<net.minecraft.item.Item> BLOCKS_SUNLIGHT = TagKey.of(
                    RegistryKeys.ITEM,
                    Thermia.modIdentifier("blocks_sunlight")
            );

            public static class BlocksSunlight {
                public static final TagKey<net.minecraft.item.Item> ANY = TagKey.of(
                        RegistryKeys.ITEM,
                        Thermia.modIdentifier("blocks_sunlight/any")
                );

                public static final TagKey<net.minecraft.item.Item> BODY = TagKey.of(
                        RegistryKeys.ITEM,
                        Thermia.modIdentifier("blocks_sunlight/body")
                );

                public static final TagKey<net.minecraft.item.Item> FEET = TagKey.of(
                        RegistryKeys.ITEM,
                        Thermia.modIdentifier("blocks_sunlight/feet")
                );

                public static final TagKey<net.minecraft.item.Item> HANDS = TagKey.of(
                        RegistryKeys.ITEM,
                        Thermia.modIdentifier("blocks_sunlight/hands")
                );

                public static final TagKey<net.minecraft.item.Item> HEAD = TagKey.of(
                        RegistryKeys.ITEM,
                        Thermia.modIdentifier("blocks_sunlight/HEAD")
                );

                public static final TagKey<net.minecraft.item.Item> LEGS = TagKey.of(
                        RegistryKeys.ITEM,
                        Thermia.modIdentifier("blocks_sunlight/legs")
                );

                public static final TagKey<net.minecraft.item.Item> MAINHAND = TagKey.of(
                        RegistryKeys.ITEM,
                        Thermia.modIdentifier("blocks_sunlight/mainhand")
                );

                public static final TagKey<net.minecraft.item.Item> OFFHAND = TagKey.of(
                        RegistryKeys.ITEM,
                        Thermia.modIdentifier("blocks_sunlight/offhand")
                );
            }
        }
    }

    public static class Enchantment {
        public static final TagKey<net.minecraft.enchantment.Enchantment> PROVIDES_CHILL = TagKey.of(
                RegistryKeys.ENCHANTMENT,
                Thermia.modIdentifier("provides_chill")
        );
        public static final TagKey<net.minecraft.enchantment.Enchantment> PROVIDES_WARMTH = TagKey.of(
                RegistryKeys.ENCHANTMENT,
                Thermia.modIdentifier("provides_warmth")
        );

        public static final TagKey<net.minecraft.enchantment.Enchantment> HYPOTHERMIA_PROTECTION = TagKey.of(
                RegistryKeys.ENCHANTMENT,
                Thermia.modIdentifier("hypothermia_protection")
        );

        public static final TagKey<net.minecraft.enchantment.Enchantment> HYPERTHERMIA_PROTECTION = TagKey.of(
                RegistryKeys.ENCHANTMENT,
                Thermia.modIdentifier("hyperthermia_protection")
        );
    }
}
