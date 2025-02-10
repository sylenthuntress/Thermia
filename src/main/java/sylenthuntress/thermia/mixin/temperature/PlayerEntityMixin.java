package sylenthuntress.thermia.mixin.temperature;

import com.mojang.authlib.GameProfile;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import sylenthuntress.thermia.registry.ThermiaAttachmentTypes;
import sylenthuntress.thermia.registry.ThermiaStatusEffects;
import sylenthuntress.thermia.temperature.GrantedThermoregulation;
import sylenthuntress.thermia.temperature.TemperatureHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity {
    @Unique
    protected boolean thermia$applyThermoregulation = false;

    protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @SuppressWarnings("UnstableApiUsage")
    @Inject(
            method = "<init>",
            at = @At("TAIL")
    )
    private void thermia$allowThermoregulation(World world, BlockPos pos, float yaw, GameProfile gameProfile, CallbackInfo ci) {
        if (world.isClient()) {
            return;
        }

        // Apply thermoregulation on first join
        final List<UUID> grantedThermoregulationToList = world.getAttachedOrCreate(
                ThermiaAttachmentTypes.GRANTED_THERMOREGULATION
        ).playerUUIDs().stream().map(UUID::fromString).toList();

        if (grantedThermoregulationToList.contains(gameProfile.getId())) {
            return;
        }

        thermia$applyThermoregulation = true;
        world.setAttached(
                ThermiaAttachmentTypes.GRANTED_THERMOREGULATION,
                GrantedThermoregulation.addPlayer(
                        new ArrayList<>(grantedThermoregulationToList.stream().map(UUID::toString).toList()),
                        gameProfile
                )
        );
    }

    @Inject(
            method = "tick",
            at = @At("HEAD")
    )
    private void thermia$applyThermoregulation(CallbackInfo ci) {
        if (!thermia$applyThermoregulation) {
            return;
        }

        thermia$applyThermoregulation = false;

        this.addStatusEffect(
                new StatusEffectInstance(
                        ThermiaStatusEffects.THERMOREGULATION,
                        6000,
                        0,
                        false,
                        false,
                        true
                ),
                null
        );
    }

    @ModifyVariable(
            method = "addExhaustion",
            at = @At("HEAD"),
            argsOnly = true
    )
    private float thermia$modifyExhaustion(float exhaustion) {
        if (TemperatureHelper.getTemperatureManager((PlayerEntity) (Object) this).isHyperthermic())
            exhaustion *= 2;
        return exhaustion;
    }
}
