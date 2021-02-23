package com.magmaguy.elitemobs.config.dungeonpackager.premade;

import com.magmaguy.elitemobs.config.dungeonpackager.DungeonPackagerConfigFields;
import com.magmaguy.elitemobs.utils.DiscordLinks;
import org.bukkit.World;
import org.bukkit.util.Vector;

import java.util.Arrays;

public class SewersMinidungeon extends DungeonPackagerConfigFields {
    public SewersMinidungeon() {
        super("sewers_minidungeon",
                false,
                "&8The Sewers",
                DungeonLocationType.WORLD,
                Arrays.asList("&fThe biggest minidungeon ever made!",
                        "&6Credits: MagmaGuy & 69OzCanOfBepis"),
                Arrays.asList(""),
                Arrays.asList(""),
                DiscordLinks.freeMinidungeons,
                DungeonSizeCategory.MINIDUNGEON,
                "em_sewer_maze",
                null,
                World.Environment.NORMAL,
                true,
                null,
                null,
                new Vector(0, 0, 0),
                90D,
                0D,
                2,
                "Difficulty: &6Medium\n" +
                        "$bossCount bosses, from level $lowestTier to $highestTier\n" +
                        "&6A complex dungeon maze with a challenging sections!",
                "&8[EM] &5This place looks abandoned... &dYet something lurks in these sewers!",

                "&8[EM] &5You managed to stay above water. &dMaybe next time you'll drown.");
    }
}
