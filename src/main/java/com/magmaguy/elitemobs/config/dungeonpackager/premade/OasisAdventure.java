package com.magmaguy.elitemobs.config.dungeonpackager.premade;

import com.magmaguy.elitemobs.config.dungeonpackager.DungeonPackagerConfigFields;
import com.magmaguy.elitemobs.utils.DiscordLinks;
import org.bukkit.World;
import org.bukkit.util.Vector;

import java.util.Arrays;

public class OasisAdventure extends DungeonPackagerConfigFields {
    public OasisAdventure() {
        super("oasis_adventure",
                false,
                "&6The Oasis",
                DungeonLocationType.WORLD,
                Arrays.asList("&fA massive adventure for beginners!",
                        "&6Credits: 69OzCanOfBepis"),
                null,
                null,
                DiscordLinks.premiumMinidungeons,
                DungeonSizeCategory.ADVENTURE,
                "em_oasis",
                null,
                World.Environment.NORMAL,
                true,
                new Vector(0, 0, 0),
                new Vector(0, 0, 0),
                new Vector(0, 0, 0),
                0D,
                0D,
                0,
                "Difficulty: &6Medium\n" +
                        "$bossCount bosses, from level $lowestTier to $highestTier\n" +
                        "&6A big adventure for beginners!",
                "&8[EM] &6You have arrived at the Oasis!",
                "&8[EM] &6You have left the Oasis!");
    }
}
