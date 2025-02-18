package sylenthuntress.thermia.data;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricAdvancementProvider;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.advancement.AdvancementFrame;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import sylenthuntress.thermia.Thermia;
import sylenthuntress.thermia.data.advancement.criterion.FreezeCriterion;
import sylenthuntress.thermia.data.advancement.criterion.OverheatCriterion;
import sylenthuntress.thermia.registry.ThermiaItems;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class AdvancementsProvider extends FabricAdvancementProvider {
    protected AdvancementsProvider(FabricDataOutput dataGen, CompletableFuture<RegistryWrapper.WrapperLookup> registryLookup) {
        super(dataGen, registryLookup);
    }

    @Override
    public void generateAdvancement(RegistryWrapper.WrapperLookup wrapperLookup, Consumer<AdvancementEntry> consumer) {
        AdvancementEntry rootAdvancement = Advancement.Builder.create()
                .display(
                        ThermiaItems.THERMIA_ICON,
                        Text.translatable("advancements.thermia.root.title"),
                        Text.translatable("advancements.thermia.root.description"),
                        Identifier.ofVanilla("textures/block/powder_snow.png"),
                        AdvancementFrame.TASK,
                        true,
                        true,
                        false
                )
                .criterion("got_hypothermia", FreezeCriterion.Conditions.create())
                .criterion("got_hyperthermia", OverheatCriterion.Conditions.create())
                .build(consumer, Thermia.MOD_ID + "/root");
        generateColdAdvancements(consumer, rootAdvancement);
        generateHotAdvancements(consumer, rootAdvancement);
    }

    protected void generateColdAdvancements(Consumer<AdvancementEntry> consumer, AdvancementEntry rootAdvancement) {
        Advancement.Builder.create()
                .parent(rootAdvancement)
                .display(
                        Items.POWDER_SNOW_BUCKET,
                        Text.translatable("advancements.thermia.got_hypothermia.title"),
                        Text.translatable("advancements.thermia.got_hypothermia.description"),
                        null,
                        AdvancementFrame.TASK,
                        true,
                        true,
                        false
                )
                .criterion("got_hypothermia", FreezeCriterion.Conditions.create())
                .build(consumer, Thermia.MOD_ID + "/got_hypothermia");
    }

    protected void generateHotAdvancements(Consumer<AdvancementEntry> consumer, AdvancementEntry rootAdvancement) {
        Advancement.Builder.create()
                .parent(rootAdvancement)
                .display(
                        Items.MAGMA_BLOCK,
                        Text.translatable("advancements.thermia.got_hyperthermia.title"),
                        Text.translatable("advancements.thermia.got_hyperthermia.description"),
                        null,
                        AdvancementFrame.TASK,
                        true,
                        true,
                        false
                )
                .criterion("got_hyperthermia", OverheatCriterion.Conditions.create())
                .build(consumer, Thermia.MOD_ID + "/got_hyperthermia");
    }
}