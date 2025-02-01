package sylenthuntress.thermia.registry.effects;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.server.world.ServerWorld;

public class HyperpyrexiaEffect extends StatusEffect {
    public HyperpyrexiaEffect(StatusEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    public boolean applyUpdateEffect(ServerWorld world, LivingEntity entity, int amplifier) {
        entity.damage(world, entity.getDamageSources().onFire(), 0.1F);
        return true;
    }



    @Override
    public boolean canApplyUpdateEffect(int duration, int amplifier) {
        int damageInterval = 25 >> amplifier;
        return damageInterval == 0 || duration % damageInterval == 0;
    }
}
