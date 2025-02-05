package sylenthuntress.thermia.mixin.client.temperature;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import sylenthuntress.thermia.registry.ThermiaStatusEffects;
import sylenthuntress.thermia.temperature.TemperatureHelper;

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
                TemperatureHelper.getTemperatureManager(livingEntity).shouldBlurVision()) {
            this.setPostProcessor(field_53899);
        }
        else if (postProcessorId == field_53899)
            this.clearPostProcessor();
    }

    @WrapOperation(method = "render", at = @At(value = "NEW", target = "(Lnet/minecraft/client/MinecraftClient;Lnet/minecraft/client/render/VertexConsumerProvider$Immediate;)Lnet/minecraft/client/gui/DrawContext;"))
    private DrawContext thermia$renderRedVision(MinecraftClient client, VertexConsumerProvider.Immediate vertexConsumers, Operation<DrawContext> original) {
        DrawContext context = original.call(client, vertexConsumers);
        if (client.player != null && TemperatureHelper.getTemperatureManager(client.player).isHypothermic()) {
            int color = MathHelper.hsvToArgb(
                    0,
                    0,
                    1,
                    15
            );
            context.fillGradient(
                    0,
                    0,
                    client.getWindow().getWidth(),
                    client.getWindow().getWidth(),
                    color,
                    color
            );
        }
        return context;
    }

    @ModifyExpressionValue(method = "renderWorld", at = @At(value = "FIELD", target = "Lnet/minecraft/client/network/ClientPlayerEntity;nauseaIntensity:F"))
    private float thermia$wobbleVision(float original) {
        return TemperatureHelper.getTemperatureManager(this.client.player).shouldDoWobble()
                ? Math.max(0.1F, original)
                : original;
    }
}
