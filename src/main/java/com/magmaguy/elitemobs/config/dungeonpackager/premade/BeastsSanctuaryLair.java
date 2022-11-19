package com.magmaguy.elitemobs.config.dungeonpackager.premade;

import com.magmaguy.elitemobs.config.dungeonpackager.DungeonPackagerConfigFields;
import com.magmaguy.elitemobs.utils.DiscordLinks;
import org.bukkit.World;

import java.util.Arrays;

public class BeastsSanctuaryLair extends DungeonPackagerConfigFields {
    public BeastsSanctuaryLair() {
        super("beasts_sanctuary_lair",
                false,
                "&6The Beasts Sanctuary",
                Arrays.asList("&6Confront the beasts!"),
                DiscordLinks.premiumMinidungeons,
                DungeonSizeCategory.LAIR,
                "em_beasts_sanctuary",
                World.Environment.NORMAL,
                true,
                "em_beasts_sanctuary,32.5,86.5,40.5,0,0",
                0,
                "Difficulty: &cHard\n" +
                        "$bossCount level $lowestTier Big Boss!\n" +
                        "&6Face fierce beasts at their home turf!",
                "&8[EM] &aYou have entered the Beast Sanctuary! Beware of what prowls here!",
                "&8[EM] &aYou've left the Beast Sanctuary! Did you take trophies?",
                "the_beasts_sanctuary");
    }
}
