package sylenthuntress.thermia.temperature;

import net.minecraft.util.Identifier;
import sylenthuntress.thermia.Thermia;

import java.util.ArrayList;

public class TemperatureModifierContainer {
    protected ArrayList<TemperatureModifier> modifiers = new ArrayList<>();

    public boolean removeModifier(Identifier id) {
        int index = 0;
        for (TemperatureModifier modifier : modifiers) {
            if (modifier.idMatches(id)) {
                modifiers.remove(index);
                return true;
            }
            index++;
        }
        return false;
    }

    public boolean removeModifiers(Identifier... modifiers) {
        boolean bl = false;
        for (Identifier modifier : modifiers) {
            removeModifier(modifier);
            bl = true;
        }
        return bl;
    }

    public boolean removeThermiaModifiers(String... modifiers) {
        boolean bl = false;
        for (String modifierName : modifiers) {
            removeModifier(Thermia.modIdentifier(modifierName));
            bl = true;
        }
        return bl;
    }

    public boolean addModifier(TemperatureModifier modifier) {
        if (!hasModifier(modifier.id())) {
            modifiers.add(modifier);
            return true;
        }
        return false;
    }

    public boolean addModifiers(TemperatureModifier... modifiers) {
        boolean bl = false;
        for (TemperatureModifier modifier : modifiers) {
            addModifier(modifier);
            bl = true;
        }
        return bl;
    }

    public boolean hasModifier(Identifier id) {
        return modifiers.stream().anyMatch((modifier -> modifier.idMatches(id)));
    }

    public TemperatureModifier getModifier(Identifier id) {
        for (TemperatureModifier modifier : modifiers) {
            if (modifier.idMatches(id))
                return modifier;
        }
        return null;
    }

    public ArrayList<TemperatureModifier> getList() {
        return modifiers;
    }
}
