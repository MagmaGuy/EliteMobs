package com.magmaguy.elitemobs.config.dungeonpackager.premade;

import com.magmaguy.elitemobs.config.dungeonpackager.DungeonPackagerConfigFields;
import com.magmaguy.elitemobs.utils.DiscordLinks;
import org.bukkit.World;

import java.util.List;

public class AdventurersGuildHub extends DungeonPackagerConfigFields {
    public AdventurersGuildHub() {
        super("adventurers_guild_hub",
                true,
                "&2[000] Adventurers Guild Hub!",
                List.of("&aThe EliteMobs hub with shops and teleports!"),
                DiscordLinks.freeMinidungeons,
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
    }
}