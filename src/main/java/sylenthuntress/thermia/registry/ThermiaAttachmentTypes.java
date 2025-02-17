package sylenthuntress.thermia.registry;

import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import sylenthuntress.thermia.Thermia;
import sylenthuntress.thermia.temperature.GrantedThermoregulation;
import sylenthuntress.thermia.temperature.TargetTemperature;
import sylenthuntress.thermia.temperature.Temperature;

@SuppressWarnings("UnstableApiUsage")
public class ThermiaAttachmentTypes {
    public static final AttachmentType<Temperature> TEMPERATURE = AttachmentRegistry.create(
            Thermia.modIdentifier("temperature"),
            builder -> builder
                    .initializer(() -> Temperature.DEFAULT)
                    .persistent(Temperature.CODEC)
                    .syncWith(
                            Temperature.PACKET_CODEC,
                            AttachmentSyncPredicate.all()
                    )
    );
    public static final AttachmentType<TargetTemperature> TARGET_TEMPERATURE = AttachmentRegistry.create(
            Thermia.modIdentifier("target_temperature"),
            builder -> builder
                    .initializer(() -> TargetTemperature.DEFAULT)
                    .persistent(TargetTemperature.CODEC)
                    .syncWith(
                            TargetTemperature.PACKET_CODEC,
                            AttachmentSyncPredicate.all()
                    )
    );
    public static final AttachmentType<GrantedThermoregulation> GRANTED_THERMOREGULATION = AttachmentRegistry.create(
            Thermia.modIdentifier("granted_thermoregulation"),
            builder -> builder
                    .initializer(() -> GrantedThermoregulation.DEFAULT)
                    .persistent(GrantedThermoregulation.CODEC)
                    .syncWith(
                            GrantedThermoregulation.PACKET_CODEC,
                            AttachmentSyncPredicate.all()
                    )
    );

    public static void registerAll() {

    }
}
