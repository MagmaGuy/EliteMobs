package com.magmaguy.elitemobs.config.contentpackages.premade;

import com.magmaguy.elitemobs.config.contentpackages.ContentPackagesConfigFields;
import org.bukkit.World;

import java.util.List;

public class BeastsSanctuaryLair extends ContentPackagesConfigFields {
    public BeastsSanctuaryLair() {
        super("beasts_sanctuary_lair",
                true,
                "&2[lvl 130] &6The Beasts Sanctuary",
                List.of("&6Confront the beasts!"),
                "https://nightbreak.io/plugin/elitemobs/#the-beasts-sanctuary",
                DungeonSizeCategory.LAIR,
                "em_beasts_sanctuary",
                World.Environment.NORMAL,
                true,
                "em_beasts_sanctuary,32.5,88.2,36.5,0,0",
                0,
                "Difficulty: &cHard\n" +
                        "$bossCount level $lowestTier Big Boss!\n" +
                        "&6Face fierce beasts at their home turf!",
                "&8[EM] &aYou have entered the Beast Sanctuary! Beware of what prowls here!",
                "&8[EM] &aYou've left the Beast Sanctuary! Did you take trophies?",
                "the_beasts_sanctuary",
                false);
        setSetupMenuDescription(List.of(
                "&2A Lair for players around level 130!"));
    }
}
