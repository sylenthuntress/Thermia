package sylenthuntress.thermia.mixin.client.temperature;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.advancement.AdvancementTab;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import sylenthuntress.thermia.Thermia;

import java.util.function.Function;

@Mixin(AdvancementTab.class)
public class AdvancementTabMixin {
    @Shadow
    private double originY;

    @WrapOperation(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/DrawContext;drawTexture(Ljava/util/function/Function;Lnet/minecraft/util/Identifier;IIFFIIII)V"
            )
    )
    private void thermia$s(
            DrawContext instance,
            Function<Identifier, RenderLayer> renderLayers,
            Identifier sprite,
            int x,
            int y,
            float u,
            float v,
            int width,
            int height,
            int textureWidth,
            int textureHeight,
            Operation<Void> original) {
        if (y > this.originY + 16) {
            sprite = Thermia.modIdentifier("textures/advancements/background/magma_block.png");
        } else if (y > this.originY) {
            sprite = Thermia.modIdentifier("textures/advancements/background/stitched.png");
        }

        original.call(instance, renderLayers, sprite, x, y, u, v, width, height, textureWidth, textureHeight);
    }
}
