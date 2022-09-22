package com.magmaguy.elitemobs.config.dungeonpackager.premade;

import com.magmaguy.elitemobs.config.dungeonpackager.DungeonPackagerConfigFields;
import com.magmaguy.elitemobs.utils.DiscordLinks;
import org.bukkit.World;

import java.util.Arrays;

public class SewersMinidungeon extends DungeonPackagerConfigFields {
    public SewersMinidungeon() {
        super("sewers_minidungeon",
                false,
                "&8The Sewers",
                Arrays.asList("&fThe biggest minidungeon ever made!",
                        "&6Credits: MagmaGuy & 69OzCanOfBepis"),
                DiscordLinks.freeMinidungeons,
                DungeonSizeCategory.MINIDUNGEON,
                "em_sewer_maze",
                World.Environment.NORMAL,
                true,
                "em_sewer_maze,-24,167,-173,90,0",
                0,
                "Difficulty: &6Medium\n" +
                        "$bossCount bosses, from level $lowestTier to $highestTier\n" +
                        "&6A complex dungeon maze with a challenging sections!",
                "&8[EM] &5This place looks abandoned... &dYet something lurks in these sewers!",

                "&8[EM] &5You managed to stay above water. &dMaybe next time you'll drown.",
                "the_sewers");
    }
}
