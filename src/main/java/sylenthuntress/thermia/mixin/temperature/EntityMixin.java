package sylenthuntress.thermia.mixin.temperature;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import sylenthuntress.thermia.registry.ThermiaEffects;

@Mixin(Entity.class)
public class EntityMixin {
    @ModifyReturnValue(method = "isFrozen", at = @At("RETURN"))
    private boolean thermia$setFrozen(boolean original) {
        if ((Entity) (Object) this instanceof LivingEntity livingEntity)
            original = original || livingEntity.hasStatusEffect(ThermiaEffects.HYPOTHERMIA);
        return original;
    }
}
