package sylenthuntress.thermia.temperature;

import net.minecraft.util.Identifier;
import net.minecraft.util.StringIdentifiable;

public record TemperatureModifier(Identifier id, double value, TemperatureModifier.Operation operation) {
    public boolean idMatches(Identifier id) {
        return id.equals(this.id);
    }
    public enum Operation implements StringIdentifiable {
        ADD_VALUE("add_value", 0),
        ADD_MULTIPLIED_VALUE("add_multiplied_value", 0);

        private final String name;
        private final int id;

        Operation(final String name, final int id) {
            this.name = name;
            this.id = id;
        }

        public int getId() {
            return this.id;
        }

        @Override
        public String asString() {
            return this.name;
        }
    }
}
