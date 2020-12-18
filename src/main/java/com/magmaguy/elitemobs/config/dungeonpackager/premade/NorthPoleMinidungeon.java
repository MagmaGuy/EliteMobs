package com.magmaguy.elitemobs.config.dungeonpackager.premade;

import com.magmaguy.elitemobs.config.dungeonpackager.DungeonPackagerConfigFields;
import org.bukkit.World;
import org.bukkit.util.Vector;

import java.util.Arrays;

public class NorthPoleMinidungeon extends DungeonPackagerConfigFields {
    public NorthPoleMinidungeon() {
        super("north_pole_minidungeon",
                false,
                "The North Pole",
                DungeonLocationType.WORLD,
                Arrays.asList("&fThe Christmas minidungeon!",
                        "&6Credits: MagmaGuy & 69OzCanOfBepis"),
                null,
                null,
                "https://discord.gg/vRW9wXhK",
                DungeonSizeCategory.MINIDUNGEON,
                "elitemobs_northpole",
                null,
                World.Environment.NORMAL,
                true,
                null,
                null,
                new Vector(0, 0, 0),
                0D,
                0D,
                1,
                "Difficulty: &6Medium\n" +
                        "$bossCount bosses, from level $lowestTier to $highestTier\n" +
                        "&6Christmas in a snow globe!");
    }
}
