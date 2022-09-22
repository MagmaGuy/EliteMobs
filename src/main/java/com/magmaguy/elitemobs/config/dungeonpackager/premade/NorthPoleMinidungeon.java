package com.magmaguy.elitemobs.config.dungeonpackager.premade;

import com.magmaguy.elitemobs.config.dungeonpackager.DungeonPackagerConfigFields;
import com.magmaguy.elitemobs.utils.DiscordLinks;
import org.bukkit.World;

import java.util.Arrays;

public class NorthPoleMinidungeon extends DungeonPackagerConfigFields {
    public NorthPoleMinidungeon() {
        super("north_pole_minidungeon",
                false,
                "&9The North Pole",
                Arrays.asList("&fThe Christmas minidungeon!",
                        "&6Credits: MagmaGuy & 69OzCanOfBepis"),
                DiscordLinks.freeMinidungeons,
                DungeonSizeCategory.MINIDUNGEON,
                "em_north_pole",
                World.Environment.NORMAL,
                true,
                "em_north_pole,-265,42,-504,44,0",
                0,
                "Difficulty: &6Medium\n" +
                        "$bossCount bosses, from level $lowestTier to $highestTier\n" +
                        "&6Christmas in a snow globe!",
                "&8[EM] &7You have reached the North Pole! &fHave you been naughty this year?",
                "&8[EM] &7Come back and visit. &fThere are plenty of sweets and treats for next time!",
                "the_north_pole");
    }
}
