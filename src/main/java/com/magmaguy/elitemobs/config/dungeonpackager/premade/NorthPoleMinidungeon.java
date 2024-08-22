package com.magmaguy.elitemobs.config.dungeonpackager.premade;

import com.magmaguy.elitemobs.config.dungeonpackager.DungeonPackagerConfigFields;
import com.magmaguy.elitemobs.utils.DiscordLinks;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.List;

public class NorthPoleMinidungeon extends DungeonPackagerConfigFields {
    public NorthPoleMinidungeon() {
        super("north_pole_minidungeon",
                true,
                "&2[lvl 055-060] &9The North Pole",
                new ArrayList<>(List.of("&fThe Christmas minidungeon!",
                        "&6Credits: MagmaGuy & 69OzCanOfBepis")),
                DiscordLinks.freeMinidungeons,
                DungeonSizeCategory.MINIDUNGEON,
                "em_north_pole",
                World.Environment.NORMAL,
                true,
                "em_north_pole,-264.5,43.2,-503.5,47,0",
                0,
                "Difficulty: &6Medium\n" +
                        "$bossCount bosses, from level $lowestTier to $highestTier\n" +
                        "&6Christmas in a snow globe!",
                "&8[EM] &7You have reached the North Pole! &fHave you been naughty this year?",
                "&8[EM] &7Come back and visit. &fThere are plenty of sweets and treats for next time!",
                "the_north_pole",
                false);
    }
}
