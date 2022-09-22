package com.magmaguy.elitemobs.config.dungeonpackager.premade;

import com.magmaguy.elitemobs.config.dungeonpackager.DungeonPackagerConfigFields;
import com.magmaguy.elitemobs.utils.DiscordLinks;
import org.bukkit.World;

import java.util.Arrays;

public class OasisAdventure extends DungeonPackagerConfigFields {
    public OasisAdventure() {
        super("oasis_adventure",
                false,
                "&6The Oasis",
                Arrays.asList("&fA massive adventure for beginners!",
                        "&6Credits: 69OzCanOfBepis"),
                DiscordLinks.premiumMinidungeons,
                DungeonSizeCategory.ADVENTURE,
                "em_oasis",
                World.Environment.NORMAL,
                true,
                "em_oasis,261,20,284,-65,0",
                2,
                "Difficulty: &2Solo-able\n" +
                        "$bossCount bosses, from level $lowestTier to $highestTier\n" +
                        "&6A big adventure for people who know the ropes!",
                "&8[EM] &6You have arrived at the Oasis!",
                "&8[EM] &6You have left the Oasis!",
                "oasis");
    }
}
