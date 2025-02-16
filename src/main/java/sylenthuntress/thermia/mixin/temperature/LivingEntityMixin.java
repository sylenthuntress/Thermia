package sylenthuntress.thermia.mixin.temperature;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.AttributeContainer;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.projectile.thrown.SnowballEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
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
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import sylenthuntress.thermia.access.LivingEntityAccess;
import sylenthuntress.thermia.registry.ThermiaAttributes;
import sylenthuntress.thermia.registry.ThermiaComponents;
import sylenthuntress.thermia.registry.ThermiaStatusEffects;
import sylenthuntress.thermia.registry.ThermiaTags;
import sylenthuntress.thermia.registry.data_components.TemperatureModifiersComponent;
import sylenthuntress.thermia.temperature.TemperatureHelper;
import sylenthuntress.thermia.temperature.TemperatureManager;

import java.util.Map;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity implements LivingEntityAccess {
    @Unique
    private TemperatureManager thermia$temperatureManager;

    public LivingEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @ModifyReturnValue(
            method = "createLivingAttributes",
            at = @At("RETURN")
    )
    private static DefaultAttributeContainer.Builder thermia$addAttributes(DefaultAttributeContainer.Builder original) {
        return original
                .add(ThermiaAttributes.BASE_TEMPERATURE)
                .add(ThermiaAttributes.COLD_OFFSET_THRESHOLD)
                .add(ThermiaAttributes.HEAT_OFFSET_THRESHOLD);
    }

    @ModifyReturnValue(
            method = "canFreeze",
            at = @At("RETURN")
    )
    private boolean thermia$applyFrostResistance(boolean original) {
        return original
                && !this.hasStatusEffect(ThermiaStatusEffects.FROST_RESISTANCE);
    }

    @Shadow
    public abstract AttributeContainer getAttributes();

    @Shadow
    public abstract boolean isInvulnerableTo(ServerWorld world, DamageSource source);

    @Shadow
    public abstract boolean hasStatusEffect(RegistryEntry<StatusEffect> effect);

    @Shadow
    public abstract ItemStack getEquippedStack(EquipmentSlot slot);

    @Shadow
    public abstract double getAttributeValue(RegistryEntry<EntityAttribute> attribute);

    @Shadow
    public abstract double getAttributeBaseValue(RegistryEntry<EntityAttribute> attribute);

    public TemperatureManager thermia$getTemperatureManager() {
        return thermia$temperatureManager;
    }

    @Unique
    private void thermia$setAttributeBase(RegistryEntry<EntityAttribute> attribute, double newBase) {
        EntityAttributeInstance entityAttributeInstance = this.getAttributes().getCustomInstance(attribute);
        if (entityAttributeInstance != null)
            entityAttributeInstance.setBaseValue(newBase);
    }

    @Inject(
            method = "<init>",
            at = @At("TAIL")
    )
    private void thermia$setTemperatureManager(EntityType<? extends LivingEntity> entityType, World world, CallbackInfo ci) {
        thermia$temperatureManager = new TemperatureManager((LivingEntity) (Object) this);
    }

    @Inject(
            method = "tick",
            at = @At("TAIL")
    )
    private void thermia$calculateTemperature(CallbackInfo ci) {
        if (this.getWorld().isClient()) {
            if (thermia$temperatureManager.doHeatEffects() && this.age % 6 == 0) {
                this.getWorld().addParticle(
                        ParticleTypes.FALLING_WATER,
                        true,
                        true,
                        this.getParticleX(0.5),
                        this.getRandomBodyY(),
                        this.getParticleZ(0.5),
                        0.0,
                        0.0,
                        0.0
                );
            }

            return;
        }

        if (this.age % 5 == 0) {
            thermia$temperatureManager.stepPassiveTemperature();
        }
    }

    @Inject(
            method = "applyDamage",
            at = @At(value = "TAIL")
    )
    private void thermia$damageInteractions(ServerWorld world, DamageSource source, float amount, CallbackInfo ci) {
        if (!this.isInvulnerableTo(world, source)) {
            TemperatureManager temperatureManager = TemperatureHelper.getTemperatureManager((LivingEntity) (Object) this);
            double[] interactionTemperatures = {0, 0};
            if (source.getAttacker() != null) {
                if (source.isIn(DamageTypeTags.IS_FREEZING))
                    interactionTemperatures[0] -= 1.5;
                if (source.isIn(DamageTypeTags.IS_FIRE))
                    interactionTemperatures[1] += 1.5;
                if (source.getAttacker().getType().isIn(ThermiaTags.EntityType.UNDEAD))
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

    @Inject(
            method = "getEquipmentChanges",
            at = @At("HEAD")
    )
    private void thermia$addTemperatureModifiers(CallbackInfoReturnable<Map<EquipmentSlot, ItemStack>> cir) {
        for (EquipmentSlot slot : EquipmentSlot.VALUES) {
            final ItemStack stack = this.getEquippedStack(slot);

            for (TemperatureModifiersComponent.Entry entry : stack.getOrDefault(
                    ThermiaComponents.TEMPERATURE_MODIFIERS,
                    TemperatureModifiersComponent.DEFAULT
            ).modifiers()) {
                if (!entry.slot().matches(slot)) {
                    continue;
                }

                thermia$temperatureManager.getTemperatureModifiers().addGrantedModifier(
                        entry.modifier()
                );
            }
        }
    }

    @Inject(
            method = "onEquipmentRemoved",
            at = @At("TAIL")
    )
    private void thermia$removeTemperatureModifiers(ItemStack removedEquipment, EquipmentSlot slot, AttributeContainer container, CallbackInfo ci) {
        for (TemperatureModifiersComponent.Entry entry : removedEquipment.getOrDefault(
                ThermiaComponents.TEMPERATURE_MODIFIERS,
                TemperatureModifiersComponent.DEFAULT
        ).modifiers()) {
            if (!entry.slot().matches(slot)) {
                continue;
            }

            thermia$temperatureManager.getTemperatureModifiers().removeModifier(
                    entry.modifier().id().withPrefixedPath("granted/")
            );
        }
    }

    @ModifyReturnValue(
            method = "canFreeze",
            at = @At("RETURN")
    )
    private boolean thermia$applyInsulation(boolean original) {
        return original && this.getAttributeValue(ThermiaAttributes.COLD_OFFSET_THRESHOLD)
                < 8 + this.getAttributeBaseValue(ThermiaAttributes.COLD_OFFSET_THRESHOLD);
    }
}