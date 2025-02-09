package sylenthuntress.thermia.mixin.temperature;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.mob.CreeperEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import sylenthuntress.thermia.registry.ThermiaStatusEffects;

import java.util.Collection;

@Mixin(CreeperEntity.class)
public class CreeperEntityMixin {
    @ModifyExpressionValue(
            method = "spawnEffectsCloud",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/mob/CreeperEntity;getStatusEffects()Ljava/util/Collection;"
            )
    )
    private Collection<StatusEffectInstance> thermia$cancelClimateEffectsCloud(Collection<StatusEffectInstance> original) {
        original.removeIf(effect ->
                effect.getEffectType().matches(key ->
                        ThermiaStatusEffects.HYPOTHERMIA.matchesKey(key)
                                || ThermiaStatusEffects.HYPERTHERMIA.matchesKey(key)
                )
        );

        return original;
    }
}
