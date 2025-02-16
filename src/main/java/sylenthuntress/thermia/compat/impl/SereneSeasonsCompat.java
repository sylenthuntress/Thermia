package sylenthuntress.thermia.compat.impl;

import net.minecraft.world.World;
import sereneseasons.api.season.ISeasonState;
import sereneseasons.api.season.SeasonHelper;
import sylenthuntress.thermia.compat.SereneSeasonsCompatBase;

public class SereneSeasonsCompat implements SereneSeasonsCompatBase {
    @Override
    public ISeasonState getSeasonState(World world) {
        return SeasonHelper.getSeasonState(world);
    }
}
