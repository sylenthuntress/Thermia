package sylenthuntress.thermia.mixin.temperature;

import net.minecraft.component.ComponentHolder;
import net.minecraft.component.ComponentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.MergedComponentMap;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.item.consume.ApplyEffectsConsumeEffect;
import net.minecraft.registry.tag.TagKey;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import sylenthuntress.thermia.registry.ThermiaComponents;
import sylenthuntress.thermia.registry.ThermiaStatusEffects;
import sylenthuntress.thermia.registry.ThermiaTags;
import sylenthuntress.thermia.registry.data_components.ConsumableTemperatureComponent;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin implements ComponentHolder {
    @Shadow
    public abstract boolean isIn(TagKey<Item> tag);

    @Shadow
    @Nullable
    public abstract <T> T set(ComponentType<? super T> type, @Nullable T value);

    @Inject(
            method = "<init>(Lnet/minecraft/item/ItemConvertible;ILnet/minecraft/component/MergedComponentMap;)V",
            at = @At("TAIL")
    )
    private void thermia$applyDefaultComponents(ItemConvertible item, int count, MergedComponentMap components, CallbackInfo ci) {
        if (!this.contains(DataComponentTypes.CONSUMABLE) && !this.contains(DataComponentTypes.EQUIPPABLE)) {
            return;
        }

        if (this.contains(DataComponentTypes.CONSUMABLE)
                && !this.contains(ThermiaComponents.CONSUMABLE_TEMPERATURE_COMPONENT)) {
            final var component = this.get(DataComponentTypes.CONSUMABLE);

            this.set(
                    ThermiaComponents.CONSUMABLE_TEMPERATURE_COMPONENT,
                    new ConsumableTemperatureComponent(thermia$calculateConsumableTemperatures())
            );

            if (this.isIn(ThermiaTags.Item.Consumable.APPLIES_FROST_RESISTANCE)) {
                @SuppressWarnings("DataFlowIssue") final var consumeEffects = component.onConsumeEffects();

                int duration = consumeEffects.stream().mapToInt(consumeEffect -> {
                    if (consumeEffect instanceof ApplyEffectsConsumeEffect consumeStatusEffect) {
                        return consumeStatusEffect.effects().stream().mapToInt(effect
                                -> effect.getEffectType() == StatusEffects.FIRE_RESISTANCE
                                ? effect.getDuration()
                                : 0
                        ).sum();
                    }

                    return 0;
                }).sum();

                if (duration == 0) {
                    duration = 1200;
                }

                consumeEffects.add(
                        new ApplyEffectsConsumeEffect(
                                new StatusEffectInstance(
                                        ThermiaStatusEffects.FROST_RESISTANCE,
                                        duration
                                )
                        )
                );
            }
        }
    }

    @Unique
    private double[] thermia$calculateConsumableTemperatures() {
        double[] temperatures = {0, 0, 0};

        if (this.isIn(ThermiaTags.Item.Consumable.COLD_FOODS)) {
            temperatures[0] -= 2;
        }
        if (this.isIn(ThermiaTags.Item.Consumable.REFRESHING_FOODS)) {
            temperatures[0] -= 0.5;
        }
        if (this.isIn(ThermiaTags.Item.Consumable.WARM_FOODS)) {
            temperatures[0] += 0.5;
        }
        if (this.isIn(ThermiaTags.Item.Consumable.HOT_FOODS)) {
            temperatures[0] += 2;
        }

        return temperatures;
    }
}
