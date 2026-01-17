package com.magmaguy.elitemobs.config.contentpackages.premade;

import com.magmaguy.elitemobs.config.contentpackages.ContentPackagesConfigFields;
import org.bukkit.World;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BeastsSanctuaryLair extends ContentPackagesConfigFields {
    public BeastsSanctuaryLair() {
        super("beasts_sanctuary_lair",
                true,
                "&2The Beasts Sanctuary",
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
        setContentType(ContentType.DYNAMIC_DUNGEON);
        setContentLevel(-1);
        setDifficulties(List.of(
                Map.of("name", "normal", "levelSync", "+5", "id", 0),
                Map.of("name", "hard", "levelSync", "+0", "id", 1),
                Map.of("name", "mythic", "levelSync", "-5", "id", 2)));
        setDungeonLockoutMinutes(1440);
        setSetupMenuDescription(List.of(
                "&2A dynamic Lair!"));
    }
}
