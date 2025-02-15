package sylenthuntress.thermia.temperature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.util.Identifier;
import net.minecraft.util.StringIdentifiable;

public record TemperatureModifier(Identifier id, double amount, TemperatureModifier.Operation operation) {
    public static final MapCodec<TemperatureModifier> MAP_CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(
                            Identifier.CODEC.fieldOf("id").forGetter(TemperatureModifier::id),
                            Codec.DOUBLE.fieldOf("amount").forGetter(TemperatureModifier::amount),
                            TemperatureModifier.Operation.CODEC.fieldOf("operation").forGetter(TemperatureModifier::operation)
                    )
                    .apply(instance, TemperatureModifier::new)
    );

    public boolean idMatches(Identifier id) {
        return id.equals(this.id);
    }

    public static boolean notGranted(Identifier id) {
        return !isGranted(id);
    }

    public static boolean isGranted(Identifier id) {
        return id.toString().startsWith("thermia:granted/");
    }

    public enum Operation implements StringIdentifiable {
        ADD_VALUE("add_value", 0, EntityAttributeModifier.Operation.ADD_VALUE),
        ADD_MULTIPLIED_VALUE("add_multiplied_value", 1, EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE);
        

        public static final Codec<TemperatureModifier.Operation> CODEC = StringIdentifiable.createCodec(TemperatureModifier.Operation::values);

        private final String name;
        private final int id;
        private final EntityAttributeModifier.Operation attributeOperation;

        Operation(final String name, final int id, EntityAttributeModifier.Operation operation) {
            this.name = name;
            this.id = id;
            this.attributeOperation = operation;
        }

        public int getId() {
            return this.id;
        }

        @Override
        public String asString() {
            return this.name;
        }

        public static Operation asTemperatureOperation(EntityAttributeModifier.Operation operation) {
            return switch (operation) {
                case ADD_VALUE -> ADD_VALUE;
                case ADD_MULTIPLIED_BASE, ADD_MULTIPLIED_TOTAL -> ADD_MULTIPLIED_VALUE;
            };
        }

        public EntityAttributeModifier.Operation asAttributeOperation() {
            return this.attributeOperation;
        }
    }
}
