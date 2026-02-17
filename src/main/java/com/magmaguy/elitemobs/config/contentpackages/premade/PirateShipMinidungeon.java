package com.magmaguy.elitemobs.config.contentpackages.premade;

import com.magmaguy.elitemobs.config.contentpackages.ContentPackagesConfigFields;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PirateShipMinidungeon extends ContentPackagesConfigFields {
    public PirateShipMinidungeon() {
        super("pirate_ship_minidungeon",
                true,
                "&2[Dynamic] &6The Pirate Ship",
                new ArrayList<>(List.of("&fA fun, challenging minidungeon full of",
                        "&fbosses made for players starting to get good,",
                        "&fat EliteMobs!",
                        "&6Credits: MagmaGuy, Realm of Lotheridon, Dali, FrostCone")),
                "https://nightbreak.io/plugin/elitemobs/#the-pirate-ship",
                DungeonSizeCategory.MINIDUNGEON,
                "em_the_pirate_ship",
                World.Environment.NORMAL,
                true,
                "em_the_pirate_ship,-85.5,65.0,243.5,180,0",
                "em_the_pirate_ship,-85.5,63.0,233.5,180,0",
                0,
                "Difficulty: &6Medium\n" +
                        "$bossCount bosses, from level $lowestTier to $highestTier\n" +
                        "&6One of the best hunting grounds for" +
                        "&6aspiring adventurers!",
                "&8[EM] &3Yarr, now boarding the Pirate Ship! &bPillage and plunder to yer' hearts content!",
                "&8[EM] &3Player overboard! &bReturn when you've earned your sea-legs ye' landlubber!",
                List.of("filename=pirate_ship_tier_45_boss_blackbeard.yml"),
                "the_pirate_ship",
                -1,
                false);
        setSetupMenuDescription(List.of(
                "&2A Minidungeon for players who love pirating!"
        ));
        setDifficulties(List.of(
                Map.of("name", "normal", "levelSync", "+5", "id", 0),
                Map.of("name", "hard", "levelSync", "0", "id", 1),
                Map.of("name", "mythic", "levelSync", "-5", "id", 2)));
        this.contentType = ContentType.DYNAMIC_DUNGEON;
        setDungeonLockoutMinutes(1440);
        setNightbreakSlug("the-pirate-ship");
    }
}
