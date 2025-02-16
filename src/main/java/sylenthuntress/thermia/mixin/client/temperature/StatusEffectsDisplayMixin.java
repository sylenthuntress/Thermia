package sylenthuntress.thermia.mixin.client.temperature;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.gui.screen.ingame.StatusEffectsDisplay;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;
import sylenthuntress.thermia.Thermia;
import sylenthuntress.thermia.registry.ThermiaStatusEffects;

import java.util.Collection;

@Mixin(StatusEffectsDisplay.class)
public class StatusEffectsDisplayMixin {
    @ModifyExpressionValue(
            method = "drawStatusEffects(Lnet/minecraft/client/gui/DrawContext;II)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/network/ClientPlayerEntity;getStatusEffects()Ljava/util/Collection;"
            )
    )
    private Collection<StatusEffectInstance> thermia(Collection<StatusEffectInstance> original) {
        original.removeIf(effect -> effect.getEffectType().matches(effectKey ->
                (ThermiaStatusEffects.HYPOTHERMIA.matchesKey(effectKey)
                        && !Thermia.CONFIG.climateEffectDisplay.SHOW_HYPOTHERMIA())
                        || (ThermiaStatusEffects.HYPERTHERMIA.matchesKey(effectKey)
                        && !Thermia.CONFIG.climateEffectDisplay.SHOW_HYPERTHERMIA())
        ));

        return original;
    }

    @ModifyArgs(method = "drawStatusEffectDescriptions", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawTextWithShadow(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/text/Text;III)I", ordinal = 0))
    private void thermia$modifyDescription(Args args, @Local StatusEffectInstance effect) {
        if (!effect.getEffectType().matches(effectKey ->
                (ThermiaStatusEffects.HYPOTHERMIA.matchesKey(effectKey)
                        && Thermia.CONFIG.climateEffectDisplay.CUSTOM_HYPOTHERMIA())
                        || (ThermiaStatusEffects.HYPERTHERMIA.matchesKey(effectKey)
                        && Thermia.CONFIG.climateEffectDisplay.CUSTOM_HYPERTHERMIA())
        )) {
            return;
        }

        if (effect.isInfinite())
            args.set(3, ((int) args.get(3)) + 6);
        if (effect.getEffectType().matches(ThermiaStatusEffects.HYPOTHERMIA::matchesKey))
            args.set(4, MathHelper.hsvToRgb(1.4F, Math.max(0, 0.6F - (effect.getAmplifier() * 0.17F)), 1F));
        else args.set(4, MathHelper.hsvToRgb(1.9F, 1F, Math.max(0.3F, 1F - (effect.getAmplifier() * 0.3F))));
    }

    @ModifyArgs(method = "drawStatusEffectDescriptions", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawTextWithShadow(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/text/Text;III)I", ordinal = 1))
    private void thermia$disableDuration(Args args, @Local StatusEffectInstance effect) {
        if (!effect.getEffectType().matches(effectKey ->
                (ThermiaStatusEffects.HYPOTHERMIA.matchesKey(effectKey)
                        && Thermia.CONFIG.climateEffectDisplay.CUSTOM_HYPOTHERMIA())
                        || (ThermiaStatusEffects.HYPERTHERMIA.matchesKey(effectKey)
                        && Thermia.CONFIG.climateEffectDisplay.CUSTOM_HYPERTHERMIA())
        ) || !effect.isInfinite()) {
            return;
        }

        args.set(1, Text.empty());
    }

    @ModifyExpressionValue(method = "getStatusEffectDescription", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/effect/StatusEffectInstance;getAmplifier()I", ordinal = 0))
    private int thermia$enableLowAmplifier(int original, StatusEffectInstance effect) {
        return !effect.getEffectType().matches(effectKey ->
                (ThermiaStatusEffects.HYPOTHERMIA.matchesKey(effectKey)
                        && Thermia.CONFIG.climateEffectDisplay.CUSTOM_HYPOTHERMIA())
                        || (ThermiaStatusEffects.HYPERTHERMIA.matchesKey(effectKey)
                        && Thermia.CONFIG.climateEffectDisplay.CUSTOM_HYPERTHERMIA())
        ) ? original : Math.max(1, original);
    }

    @ModifyExpressionValue(method = "getStatusEffectDescription", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/effect/StatusEffectInstance;getAmplifier()I", ordinal = 1))
    private int thermia$enableHighAmplifier(int original, StatusEffectInstance effect) {
        return !effect.getEffectType().matches(effectKey ->
                (ThermiaStatusEffects.HYPOTHERMIA.matchesKey(effectKey)
                        && Thermia.CONFIG.climateEffectDisplay.CUSTOM_HYPOTHERMIA())
                        || (ThermiaStatusEffects.HYPERTHERMIA.matchesKey(effectKey)
                        && Thermia.CONFIG.climateEffectDisplay.CUSTOM_HYPERTHERMIA())
        ) && effect.getAmplifier() == 10 ? original : Math.max(9, original);
    }
}
