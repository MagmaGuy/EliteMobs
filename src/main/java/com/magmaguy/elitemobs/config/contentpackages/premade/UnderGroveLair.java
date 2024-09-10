package com.magmaguy.elitemobs.config.contentpackages.premade;

import com.magmaguy.elitemobs.config.contentpackages.ContentPackagesConfigFields;
import org.bukkit.World;

import java.util.List;

public class UnderGroveLair extends ContentPackagesConfigFields {
    public UnderGroveLair() {
        super("under_grove_lair",
                true,
                "&2[lvl 170] &6The Under Grove",
                List.of("&6Stop the dryad's ritual!"),
                "https://nightbreak.io/plugin/elitemobs/#the-under-grove",
                DungeonSizeCategory.LAIR,
                "em_under_grove",
                World.Environment.NORMAL,
                true,
                "em_under_grove,-2.5,-38.8,35.5,90,0",
                0,
                "Difficulty: &cHard\n" +
                        "$bossCount level $lowestTier Big Boss!\n" +
                        "&6The dryads are up to no good, stop them!",
                "&8[EM] &aYou are now trespassing the sacred grove!",
                "&8[EM] &aYou have left the sacred grove!",
                "the_under_grove",
                false);
        setSetupMenuDescription(List.of(
                "&2A Lair for players around level 170!"
        ));
    }
}
