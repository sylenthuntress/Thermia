package sylenthuntress.thermia.mixin.temperature;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.AttributeContainer;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import sylenthuntress.thermia.Thermia;
import sylenthuntress.thermia.access.temperature.LivingEntityAccess;
import sylenthuntress.thermia.registry.ThermiaAttributes;
import sylenthuntress.thermia.registry.ThermiaTags;
import sylenthuntress.thermia.temperature.TemperatureHelper;
import sylenthuntress.thermia.temperature.TemperatureManager;

import java.util.Map;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity implements LivingEntityAccess {
    @Shadow public abstract double getAttributeBaseValue(RegistryEntry<EntityAttribute> attribute);

    @Shadow public abstract AttributeContainer getAttributes();

    @Unique
    private TemperatureManager thermia$temperatureManager;

    public LivingEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    public TemperatureManager thermia$getTemperatureManager() {
        return thermia$temperatureManager;
    }

    @Unique
    private LivingEntityMixin thermia$setAttributeBase(RegistryEntry<EntityAttribute> attribute, double newBase) {
        EntityAttributeInstance entityAttributeInstance = this.getAttributes().getCustomInstance(attribute);
        if (entityAttributeInstance != null)
            entityAttributeInstance.setBaseValue(newBase);
        return this;
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void thermia$setTemperatureManager(EntityType<? extends LivingEntity> entityType, World world, CallbackInfo ci) {
        thermia$temperatureManager = new TemperatureManager((LivingEntity) (Object) this);
        if (!this.getType().isIn(ThermiaTags.TEMPERATURE_IMMUNE)) {
            double bodyTemperature = 97;
            double coldModifier = 1;
            double heatModifier = 1;

            if (this.getType().isIn(ThermiaTags.COLD_MOBS)) {
                bodyTemperature += 10;
                heatModifier += 1;
                coldModifier -= 0.5;
            }
            if (this.getType().isIn(ThermiaTags.HOT_MOBS)) {
                bodyTemperature -= 10;
                coldModifier += 1;
                heatModifier -= 0.5;
            }
            if (this.getType().isIn(ThermiaTags.NETHER_MOBS)) {
                bodyTemperature += 30;
                coldModifier += 2;
                heatModifier -= 0.3;
            }
            if (this.getType().isIn(ThermiaTags.UNDEAD_MOBS)) {
                bodyTemperature *= 0.1;
                heatModifier *= 2;
                coldModifier *= 0.5;
            }

            thermia$setAttributeBase(ThermiaAttributes.BODY_TEMPERATURE, bodyTemperature)
                    .thermia$setAttributeBase(ThermiaAttributes.COLD_MODIFIER, coldModifier)
                    .thermia$setAttributeBase(ThermiaAttributes.HEAT_MODIFIER, heatModifier);
        }
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void thermia$calculateTemperature(CallbackInfo ci) {
        if (!this.getWorld().isClient()) {
            LivingEntity livingEntity = (LivingEntity) (Object) this;
            thermia$temperatureManager.stepPassiveTemperature();

            if (!this.isOnFire())
                TemperatureHelper.getTemperatureManager(livingEntity).getTemperatureModifiers().removeModifier(Thermia.modIdentifier("on_fire"));

            if (this.age % 100 == 0 && (Object) this instanceof PlayerEntity)
                Thermia.LOGGER.info("SERVER {} -> {}",
                        thermia$temperatureManager.getTemperature(),
                        thermia$temperatureManager.getTargetTemperature());
        } else if (this.age % 100 == 0 && (Object) this instanceof PlayerEntity playerEntity)
            Thermia.LOGGER.info("CLIENT {} -> {}",
                    thermia$temperatureManager.getTemperature(),
                    thermia$temperatureManager.getTargetTemperature());
    }

    @ModifyReturnValue(method = "createLivingAttributes", at = @At("RETURN"))
    private static DefaultAttributeContainer.Builder thermia$addAttributes(DefaultAttributeContainer.Builder original) {
        return original
                .add(ThermiaAttributes.BODY_TEMPERATURE)
                .add(ThermiaAttributes.COLD_MODIFIER)
                .add(ThermiaAttributes.HEAT_MODIFIER);
    }
}