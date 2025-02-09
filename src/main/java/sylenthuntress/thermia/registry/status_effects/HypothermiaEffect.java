package sylenthuntress.thermia.registry.status_effects;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.AttributeContainer;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.server.world.ServerWorld;
import sylenthuntress.thermia.Thermia;

public class HypothermiaEffect extends StatusEffect {
    public HypothermiaEffect(StatusEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    public boolean applyUpdateEffect(ServerWorld world, LivingEntity entity, int amplifier) {
        if (!entity.isFrozen()) {
            entity.damage(world, entity.getDamageSources().freeze(), 0.5F);
        }

        return true;
    }

    @Override
    public boolean canApplyUpdateEffect(int duration, int amplifier) {
        int damageInterval = 120 >> amplifier;
        return damageInterval == 0 || duration % damageInterval == 0;
    }

    @Override
    public void onApplied(LivingEntity entity, int amplifier) {
        super.onApplied(entity, amplifier);

        EntityAttributeInstance attribute = entity.getAttributes().getCustomInstance(EntityAttributes.MOVEMENT_SPEED);

        if (attribute != null && !attribute.hasModifier(Thermia.modIdentifier("effect.hypothermia.slowness")))
            attribute.addPersistentModifier(
                    new EntityAttributeModifier(
                            Thermia.modIdentifier("effect.hypothermia.slowness"),
                            -(0.05 * (1 + amplifier * 0.1)),
                            EntityAttributeModifier.Operation.ADD_VALUE
                    )
            );
    }

    @SuppressWarnings("DataFlowIssue")
    @Override
    public void onRemoved(AttributeContainer container) {
        container.getCustomInstance(EntityAttributes.MOVEMENT_SPEED)
                .removeModifier(Thermia.modIdentifier("effect.hypothermia.slowness"));
    }
}
