package com.magmaguy.elitemobs.config.dungeonpackager.premade;

import com.magmaguy.elitemobs.config.dungeonpackager.DungeonPackagerConfigFields;
import com.magmaguy.elitemobs.utils.DiscordLinks;
import org.bukkit.World;

import java.util.List;

public class SteamworksLair extends DungeonPackagerConfigFields {
    public SteamworksLair() {
        super("steamworks_lair",
                true,
                "&2[lvl 140] &6The Steamworks",
                List.of("&fA steampunk inspired lair!"),
                DiscordLinks.freeMinidungeons,
                DungeonSizeCategory.LAIR,
                "em_steamworks_lair",
                World.Environment.NORMAL,
                true,
                "em_steamworks_lair,-48.5,-47.8,-102.5,40,0",
                0,
                "Difficulty: &chard\n" +
                        "$bossCount level $lowestTier Big Boss!\n" +
                        "&6Get ready for a robot rumble!",
                "&6Reach the top and mind your step!",
                "&6Now leaving the clockwork dominion!",
                "the_steamworks",
                false);
    }
}
