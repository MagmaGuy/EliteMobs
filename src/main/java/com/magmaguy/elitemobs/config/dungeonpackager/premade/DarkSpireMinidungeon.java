package com.magmaguy.elitemobs.config.dungeonpackager.premade;

import com.magmaguy.elitemobs.config.dungeonpackager.DungeonPackagerConfigFields;
import com.magmaguy.elitemobs.utils.DiscordLinks;
import org.bukkit.World;

import java.util.Arrays;

public class DarkSpireMinidungeon extends DungeonPackagerConfigFields {
    public DarkSpireMinidungeon() {
        super("dark_spire_minidungeon",
                false,
                "&8The Dark Spire",
                Arrays.asList("&fThe first ever high level content!",
                        "&fMade for those who want a real challenge!",
                        "&6Credits: 69OzCanOfBepis"),
                DiscordLinks.premiumMinidungeons,
                DungeonSizeCategory.MINIDUNGEON,
                "em_dark_spire",
                World.Environment.NETHER,
                true,
                "em_dark_spire,61,96,96,153,0",
                0,
                "Difficulty: &6Medium\n" +
                        "$bossCount bosses, from level $lowestTier to $highestTier\n" +
                        "&cA vast challenge for advanced players!",
                "&8[EM] &1An invasion is in progress. &9Stop the insurrection!",
                "&8[EM] &1You managed to hold them back. &9For now...",
                "the_dark_spire");
    }
}
