package sylenthuntress.thermia.registry.status_effects;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;

public class HyperthermiaEffect extends StatusEffect {
    private boolean canDamage;

    public HyperthermiaEffect(StatusEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    public boolean applyUpdateEffect(ServerWorld world, LivingEntity entity, int amplifier) {
        if (canDamage && !entity.isOnFire()) {
            entity.damage(world, entity.getDamageSources().onFire(), 0.5F);
            canDamage = false;
        }
        else if (entity instanceof PlayerEntity player) {
            player.addExhaustion(0.01F * (float) (amplifier + 1));
        }

        return true;
    }

    @Override
    public boolean canApplyUpdateEffect(int duration, int amplifier) {
        int damageInterval = 120 >> amplifier;
        canDamage = damageInterval == 0 || duration % damageInterval == 0;
        return true;
    }
}
