package com.magmaguy.elitemobs.config.contentpackages.premade;

import com.magmaguy.elitemobs.config.contentpackages.ContentPackagesConfigFields;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.List;

public class PirateShipMinidungeon extends ContentPackagesConfigFields {
    public PirateShipMinidungeon() {
        super("pirate_ship_minidungeon",
                true,
                "&2[lvl 010-020] &6The Pirate Ship",
                new ArrayList<>(List.of("&fA fun, challenging minidungeon full of",
                        "&fbosses made for players starting to get good,",
                        "&fat EliteMobs!",
                        "&6Credits: MagmaGuy & Realm of Lotheridon")),
                "https://nightbreak.io/plugin/elitemobs/#the-pirate-ship",
                DungeonSizeCategory.MINIDUNGEON,
                "em_the_pirate_ship",
                World.Environment.NORMAL,
                true,
                "em_the_pirate_ship,-85.5,64.0,236.5,180,0",
                0,
                "Difficulty: &6Medium\n" +
                        "$bossCount bosses, from level $lowestTier to $highestTier\n" +
                        "&6One of the best hunting grounds for" +
                        "&6aspiring adventurers!",
                "&8[EM] &3Now boarding the Pirate Ship! &bPillage and plunder to yer' hearts content!",
                "&8[EM] &3Player overboard! &bReturn when you've earned your sea-legs ye' landlubber!",
                "the_pirate_ship",
                false);
        setSetupMenuDescription(List.of(
                "&2A Minidungeon for players around level 15!"        ));
    }
}
