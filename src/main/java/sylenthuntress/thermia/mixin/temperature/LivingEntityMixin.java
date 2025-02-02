package sylenthuntress.thermia.mixin.temperature;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import sylenthuntress.thermia.Thermia;
import sylenthuntress.thermia.access.temperature.LivingEntityAccess;
import sylenthuntress.thermia.registry.ThermiaAttributes;
import sylenthuntress.thermia.temperature.TemperatureHelper;
import sylenthuntress.thermia.temperature.TemperatureManager;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity implements LivingEntityAccess {
    @Unique
    private TemperatureManager thermia$temperatureManager;

    public LivingEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    public TemperatureManager thermia$getTemperatureManager() {
        return thermia$temperatureManager;
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void thermia$setTemperatureManager(EntityType<? extends LivingEntity> entityType, World world, CallbackInfo ci) {
        thermia$temperatureManager = new TemperatureManager((LivingEntity) (Object) this);
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void thermia$calculateTemperature(CallbackInfo ci) {
        if (!this.getWorld().isClient()) {
            LivingEntity livingEntity = (LivingEntity) (Object) this;
            thermia$temperatureManager.stepPassiveTemperature();

            if (!this.isOnFire())
                TemperatureHelper.removeModifier(livingEntity, Thermia.modIdentifier("on_fire"));

            if (this.age % 100 == 0 && (Object) this instanceof PlayerEntity)
                Thermia.LOGGER.info("SERVER {} -> {}",
                        thermia$temperatureManager.getTemperature(),
                        TemperatureHelper.getTargetTemperature(livingEntity));
        } else if (this.age % 100 == 0 && (Object) this instanceof PlayerEntity playerEntity)
            Thermia.LOGGER.info("CLIENT {} -> {}",
                    thermia$temperatureManager.getTemperature(),
                    TemperatureHelper.getTargetTemperature(playerEntity));
    }

    @ModifyReturnValue(method = "createLivingAttributes", at = @At("RETURN"))
    private static DefaultAttributeContainer.Builder thermia$addAttributes(DefaultAttributeContainer.Builder original) {
        return original
                .add(ThermiaAttributes.BODY_TEMPERATURE)
                .add(ThermiaAttributes.COLD_MODIFIER)
                .add(ThermiaAttributes.HEAT_MODIFIER);
    }
}