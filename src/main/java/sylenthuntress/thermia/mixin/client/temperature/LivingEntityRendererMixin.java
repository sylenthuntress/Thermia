package sylenthuntress.thermia.mixin.client.temperature;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import sylenthuntress.thermia.registry.ThermiaStatusEffects;
import sylenthuntress.thermia.temperature.TemperatureHelper;

@Mixin(LivingEntityRenderer.class)
public class LivingEntityRendererMixin {
    @ModifyExpressionValue(method = "updateRenderState(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/client/render/entity/state/LivingEntityRenderState;F)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;isFrozen()Z"))
    private boolean thermia$hypothermiaShaking(boolean original, LivingEntity entity) {
        return original || TemperatureHelper.getTemperatureManager(entity).shouldShake();
    }
}
