package sylenthuntress.thermia.registry;

import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import sylenthuntress.thermia.Thermia;
import sylenthuntress.thermia.registry.status_effects.HyperthermiaEffect;
import sylenthuntress.thermia.registry.status_effects.HypothermiaEffect;

public class ThermiaStatusEffects {
    public static final RegistryEntry<StatusEffect> HYPOTHERMIA = register(
            "hypothermia",
            new HypothermiaEffect(StatusEffectCategory.HARMFUL, 12624973)
    );
    public static final RegistryEntry<StatusEffect> HYPERTHERMIA = register(
            "hyperthermia",
            new HyperthermiaEffect(StatusEffectCategory.HARMFUL, 14367241)
    );

    private static RegistryEntry<StatusEffect> register(String id, StatusEffect statusEffect) {
        return Registry.registerReference(Registries.STATUS_EFFECT, Thermia.modIdentifier(id), statusEffect);
    }

    public static void registerAll() {

    }
}
