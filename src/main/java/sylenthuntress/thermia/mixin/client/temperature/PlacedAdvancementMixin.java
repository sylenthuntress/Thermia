package sylenthuntress.thermia.mixin.client.temperature;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.advancement.PlacedAdvancement;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import sylenthuntress.thermia.Thermia;

import java.util.Comparator;
import java.util.stream.StreamSupport;

@Mixin(PlacedAdvancement.class)
public abstract class PlacedAdvancementMixin {
    @Shadow
    @Final
    private AdvancementEntry advancementEntry;

    @ModifyReturnValue(
            method = "getChildren",
            at = @At(
                    value = "RETURN"
            )
    )
    private Iterable<PlacedAdvancement> thermia$sortThermiaChildren(Iterable<PlacedAdvancement> original) {
        if (!this.advancementEntry.id().toString().equals(Thermia.MOD_ID + ":root")) {
            return original;
        }

        return new java.util.ArrayList<>(
                StreamSupport.stream(original.spliterator(), false).sorted(Comparator.comparing(
                        advancement
                                -> advancement.getAdvancement().name().orElse(Text.empty()).getString())).toList()
        );
    }
}