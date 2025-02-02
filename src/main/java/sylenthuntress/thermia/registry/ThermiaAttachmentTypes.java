package sylenthuntress.thermia.registry;

import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import sylenthuntress.thermia.Thermia;
import sylenthuntress.thermia.temperature.Temperature;

@SuppressWarnings("UnstableApiUsage")
public class ThermiaAttachmentTypes {
    public static final AttachmentType<Temperature> TEMPERATURE = AttachmentRegistry.create(
            Thermia.modIdentifier("temperature_manager"),
            builder->builder
                    .initializer(() -> Temperature.DEFAULT)
                    .persistent(Temperature.CODEC)
                    .syncWith(
                            Temperature.PACKET_CODEC,
                            AttachmentSyncPredicate.all()
                    )
    );

    public static void init() {

    }
}
