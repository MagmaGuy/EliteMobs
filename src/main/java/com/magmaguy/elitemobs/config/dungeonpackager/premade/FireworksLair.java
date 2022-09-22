package com.magmaguy.elitemobs.config.dungeonpackager.premade;

import com.magmaguy.elitemobs.config.dungeonpackager.DungeonPackagerConfigFields;
import com.magmaguy.elitemobs.utils.DiscordLinks;
import org.bukkit.World;

import java.util.Arrays;

public class FireworksLair extends DungeonPackagerConfigFields {
    public FireworksLair() {
        super("fireworks_lair",
                false,
                "&aFireworks",
                Arrays.asList("&fThe 2021 4th of July map!",
                        "&6Credits: MagmaGuy"),
                DiscordLinks.freeMinidungeons,
                DungeonSizeCategory.LAIR,
                "em_fireworks",
                World.Environment.NORMAL,
                true,
                "em_fireworks,3,62,-20,17,-5",
                0,
                "Difficulty: &cHard\n" +
                        "$bossCount level $lowestTier Big Boss!\n" +
                        "&6An encounter full of explosions!",
                "&8[EM] &eWelcome to the fireworks show!",
                "&8[EM] &eYou've left the party!",
                "the_fireworks");
    }
}
