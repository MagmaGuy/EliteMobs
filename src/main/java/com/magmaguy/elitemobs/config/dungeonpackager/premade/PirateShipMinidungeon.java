package com.magmaguy.elitemobs.config.dungeonpackager.premade;

import com.magmaguy.elitemobs.config.dungeonpackager.DungeonPackagerConfigFields;
import com.magmaguy.elitemobs.utils.DiscordLinks;
import org.bukkit.World;

import java.util.Arrays;

public class PirateShipMinidungeon extends DungeonPackagerConfigFields {
    public PirateShipMinidungeon() {
        super("pirate_ship_minidungeon",
                true,
                "&2[lvl 010-020] &6The Pirate Ship",
                Arrays.asList("&fA fun, challenging minidungeon full of",
                        "&fbosses made for players starting to get good,",
                        "&fat EliteMobs!",
                        "&6Credits: MagmaGuy & Realm of Lotheridon"),
                DiscordLinks.freeMinidungeons,
                DungeonSizeCategory.MINIDUNGEON,
                "em_pirate_ship",
                World.Environment.NORMAL,
                true,
                "em_the_pirate_ship,-85,63,243.5,-150,0",
                0,
                "Difficulty: &6Medium\n" +
                        "$bossCount bosses, from level $lowestTier to $highestTier\n" +
                        "&6One of the best hunting grounds for" +
                        "&6aspiring adventurers!",
                "&8[EM] &3Now boarding the Pirate Ship! &bPillage and plunder to yer' hearts content!",
                "&8[EM] &3Player overboard! &bReturn when you've earned your sea-legs ye' landlubber!",
                "the_pirate_ship",
                false);
    }
}
