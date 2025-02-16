package sylenthuntress.thermia.compat;

import net.minecraft.world.World;
import sereneseasons.api.season.ISeasonState;

public interface SereneSeasonsCompatBase {
    ISeasonState getSeasonState(World world);
}
