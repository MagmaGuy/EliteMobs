package com.magmaguy.elitemobs.config.dungeonpackager.premade;

import com.magmaguy.elitemobs.config.dungeonpackager.DungeonPackagerConfigFields;
import org.bukkit.World;
import org.bukkit.util.Vector;

import java.util.Arrays;

public class DarkSpireMinidungeon extends DungeonPackagerConfigFields {
    public DarkSpireMinidungeon() {
        super("dark_spire_lair",
                false,
                "&8The Dark Spire",
                DungeonLocationType.WORLD,
                Arrays.asList("&fThe first ever high level content!",
                        "&fMade for those who want a real challenge!",
                        "&6Credits: 69OzCanOfBepis"),
                Arrays.asList(""),
                Arrays.asList(""),
                "patreon.com/magmaguy",
                DungeonSizeCategory.MINIDUNGEON,
                "elitemobs_hell_tower",
                null,
                World.Environment.NETHER,
                true,
                null,
                null,
                new Vector(0, 0, 0),
                150D,
                0D,
                1,
                "Difficulty: &cHard\n" +
                        "$bossCount bosses, from level $lowestTier to $highestTier\n" +
                        "&cA vast challenge for advanced players!");
    }
}
