package com.magmaguy.elitemobs.config.contentpackages.premade;

import com.magmaguy.elitemobs.config.contentpackages.ContentPackagesConfigFields;
import org.bukkit.World;

import java.util.List;

public class SteamworksLair extends ContentPackagesConfigFields {
    public SteamworksLair() {
        super("steamworks_lair",
                true,
                "&2[lvl 140] &6The Steamworks",
                List.of("&fA steampunk inspired lair!"),
                "https://nightbreak.io/plugin/elitemobs/#the-steamworks",
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
        setSetupMenuDescription(List.of(
                "&2A Lair for players around level 140!"
        ));
    }
}
