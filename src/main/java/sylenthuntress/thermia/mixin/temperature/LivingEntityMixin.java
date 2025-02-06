package sylenthuntress.thermia.mixin.temperature;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalBiomeTags;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.AttributeContainer;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.projectile.thrown.SnowballEntity;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.registry.tag.EntityTypeTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import sylenthuntress.thermia.Thermia;
import sylenthuntress.thermia.access.temperature.LivingEntityAccess;
import sylenthuntress.thermia.registry.ThermiaAttributes;
import sylenthuntress.thermia.registry.ThermiaTags;
import sylenthuntress.thermia.temperature.TemperatureHelper;
import sylenthuntress.thermia.temperature.TemperatureManager;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity implements LivingEntityAccess {
    @Unique
    private TemperatureManager thermia$temperatureManager;

    public LivingEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @ModifyReturnValue(method = "createLivingAttributes", at = @At("RETURN"))
    private static DefaultAttributeContainer.Builder thermia$addAttributes(DefaultAttributeContainer.Builder original) {
        return original
                .add(ThermiaAttributes.BODY_TEMPERATURE)
                .add(ThermiaAttributes.COLD_MODIFIER)
                .add(ThermiaAttributes.HEAT_MODIFIER)
                .add(ThermiaAttributes.COLD_OFFSET_THRESHOLD)
                .add(ThermiaAttributes.HEAT_OFFSET_THRESHOLD);
    }

    @Shadow
    public abstract AttributeContainer getAttributes();

    @Shadow
    public abstract boolean isInvulnerableTo(ServerWorld world, DamageSource source);

    public TemperatureManager thermia$getTemperatureManager() {
        return thermia$temperatureManager;
    }

    @Unique
    private void thermia$setAttributeBase(RegistryEntry<EntityAttribute> attribute, double newBase) {
        EntityAttributeInstance entityAttributeInstance = this.getAttributes().getCustomInstance(attribute);
        if (entityAttributeInstance != null)
            entityAttributeInstance.setBaseValue(newBase);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void thermia$setTemperatureManager(EntityType<? extends LivingEntity> entityType, World world, CallbackInfo ci) {
        thermia$temperatureManager = new TemperatureManager((LivingEntity) (Object) this);
        double coldOffsetThreshold = -2;
        double heatOffsetThreshold = 3;
        if (this.getType().isIn(ThermiaTags.COLD_MOBS))
            coldOffsetThreshold += 15;
        if (this.getType().isIn(ThermiaTags.HOT_MOBS))
            heatOffsetThreshold += 15;
        if (this.getType().isIn(ThermiaTags.NETHER_MOBS)) {
            heatOffsetThreshold += 10;
            coldOffsetThreshold += 6;
        }
        if (this.getType().isIn(ThermiaTags.UNDEAD_MOBS)) {
            heatOffsetThreshold += 20;
            coldOffsetThreshold += 20;
        }
        if (!this.isPlayer()) {
            if (world.getBiome(this.getBlockPos()).isIn(ConventionalBiomeTags.IS_COLD))
                coldOffsetThreshold += 5;
            if (world.getBiome(this.getBlockPos()).isIn(ConventionalBiomeTags.IS_DRY)) {
                heatOffsetThreshold += 5;
                coldOffsetThreshold += 5;
            }
        }
        thermia$setAttributeBase(ThermiaAttributes.COLD_OFFSET_THRESHOLD, coldOffsetThreshold);
        thermia$setAttributeBase(ThermiaAttributes.HEAT_OFFSET_THRESHOLD, heatOffsetThreshold);
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void thermia$calculateTemperature(CallbackInfo ci) {
        if (!this.getWorld().isClient()) {
            LivingEntity livingEntity = (LivingEntity) (Object) this;
            thermia$temperatureManager.stepPassiveTemperature();

            if (!this.isOnFire())
                TemperatureHelper.getTemperatureManager(livingEntity).getTemperatureModifiers().removeModifier(Thermia.modIdentifier("on_fire"));
        }
    }

    @Inject(method = "applyDamage", at = @At(value = "TAIL"))
    private void thermia$damageInteractions(ServerWorld world, DamageSource source, float amount, CallbackInfo ci) {
        if (!this.isInvulnerableTo(world, source)) {
            TemperatureManager temperatureManager = TemperatureHelper.getTemperatureManager((LivingEntity) (Object) this);
            double[] interactionTemperatures = {0, 0};
            if (source.getAttacker() != null) {
                if (source.isIn(DamageTypeTags.IS_FREEZING))
                    interactionTemperatures[0] -= 1.5;
                if (source.isIn(DamageTypeTags.IS_FIRE))
                    interactionTemperatures[1] += 1.5;
                if (source.getAttacker().getType().isIn(ThermiaTags.UNDEAD_MOBS))
                    interactionTemperatures[0] -= 0.1;
                if (source.getAttacker().getType().isIn(EntityTypeTags.FREEZE_HURTS_EXTRA_TYPES))
                    interactionTemperatures[1] += 0.5;
            } else {
                if (source.isIn(DamageTypeTags.BURN_FROM_STEPPING))
                    interactionTemperatures[1] += 0.5;
            }
            if (source.getSource() instanceof SnowballEntity)
                interactionTemperatures[0] -= 3;
            if (source.isIn(DamageTypeTags.IS_LIGHTNING))
                interactionTemperatures[1] += 10;
            temperatureManager.modifyTemperature(interactionTemperatures);
        }
    }
}