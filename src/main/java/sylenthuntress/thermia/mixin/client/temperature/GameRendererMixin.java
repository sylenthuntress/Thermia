package sylenthuntress.thermia.mixin.client.temperature;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import sylenthuntress.thermia.Thermia;
import sylenthuntress.thermia.temperature.TemperatureHelper;

import java.util.Objects;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {
    @Shadow @Final private MinecraftClient client;

    @Shadow protected abstract void setPostProcessor(Identifier id);

    @Shadow public abstract void clearPostProcessor();

    @Shadow private @Nullable Identifier postProcessorId;

    @Shadow @Final private static Identifier field_53899;

    @Inject(method = "render", at = @At("HEAD"))
    private void thermia$loadHyperthermiaShader(RenderTickCounter tickCounter, boolean tick, CallbackInfo ci) {
        if (client.getCameraEntity() instanceof LivingEntity livingEntity &&
                TemperatureHelper.getTemperatureManager(livingEntity).isHyperthermic()) {
            this.setPostProcessor(field_53899);
        }
        else if (postProcessorId == field_53899)
            this.clearPostProcessor();
    }
}
