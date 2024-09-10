package com.magmaguy.elitemobs.config.contentpackages.premade;

import com.magmaguy.elitemobs.config.contentpackages.ContentPackagesConfigFields;
import org.bukkit.World;

import java.util.List;

public class AdventurersGuildHub extends ContentPackagesConfigFields {
    public AdventurersGuildHub() {
        super("adventurers_guild_hub",
                true,
                "&2Adventurers Guild Hub!",
                List.of("&aThe EliteMobs hub with shops and teleports!"),
                "https://nightbreak.io/plugin/elitemobs/#the-adventurers-guild",
                "em_adventurers_guild",
                World.Environment.NORMAL,
                true,
                "em_adventurers_guild,208.5,88,236.5,-80,0",
                0,
                "Difficulty: &cHard\n" +
                        "$bossCount level $lowestTier Big Boss!\n" +
                        "&6Face fierce beasts at their home turf!",
                "&8[EM] &aYou have entered the Beast Sanctuary! Beware of what prowls here!",
                "&8[EM] &aYou've left the Beast Sanctuary! Did you take trophies?",
                "em_adventurers_guild",
                false);
        setSetupMenuDescription(List.of(
                "&2The highly recommended Elitemobs Hub!",
                "&2It does not replace your worlds, it just",
                "&2adds an isolated world with all the NPCs",
                "&2and portals for EliteMobs!"));
    }
}