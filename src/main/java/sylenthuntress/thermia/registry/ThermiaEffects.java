package sylenthuntress.thermia.registry;

import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import sylenthuntress.thermia.Thermia;
import sylenthuntress.thermia.registry.effects.HypothermiaEffect;

public class ThermiaEffects {
    public static final RegistryEntry<StatusEffect> HYPOTHERMIA = register(
            "hypothermia",
            new HypothermiaEffect(StatusEffectCategory.HARMFUL, 12624973)
                    .addAttributeModifier(EntityAttributes.MOVEMENT_SPEED, Identifier.of(Thermia.MOD_ID, "effect.hypothermia"), -0.05F, EntityAttributeModifier.Operation.ADD_VALUE)
                    .addAttributeModifier(EntityAttributes.ATTACK_SPEED, Identifier.of(Thermia.MOD_ID, "effect.hypothermia"), -0.05F, EntityAttributeModifier.Operation.ADD_VALUE)
    );
    public static final RegistryEntry<StatusEffect> HYPERPYREXIA = register(
            "hyperpyrexia",
            new HypothermiaEffect(StatusEffectCategory.HARMFUL, 14367241)
    );

    private static RegistryEntry<StatusEffect> register(String id, StatusEffect statusEffect) {
        return Registry.registerReference(Registries.STATUS_EFFECT, Identifier.of(Thermia.MOD_ID, id), statusEffect);
    }
    public static void registerAll() {

    }
}
