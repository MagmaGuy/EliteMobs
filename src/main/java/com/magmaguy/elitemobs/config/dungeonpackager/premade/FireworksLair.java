package com.magmaguy.elitemobs.config.dungeonpackager.premade;

import com.magmaguy.elitemobs.config.dungeonpackager.DungeonPackagerConfigFields;
import com.magmaguy.elitemobs.utils.DiscordLinks;
import org.bukkit.World;
import org.bukkit.util.Vector;

import java.util.Arrays;

public class FireworksLair extends DungeonPackagerConfigFields {
    public FireworksLair() {
        super("fireworks_lair",
                false,
                "&aFireworks",
                DungeonLocationType.WORLD,
                Arrays.asList("&fThe 2021 4th of July map!",
                        "&6Credits: MagmaGuy"),
                Arrays.asList(""),
                Arrays.asList(""),
                DiscordLinks.freeMinidungeons,
                DungeonSizeCategory.LAIR,
                "em_fireworks",
                null,
                World.Environment.NORMAL,
                true,
                null,
                null,
                new Vector(0, 0, 0),
                -130D,
                0D,
                2,
                "Difficulty: &6Medium\n" +
                        "$bossCount level $lowestTier Big Boss!\n" +
                        "&6An encounter full of explosions!",
                "&8[EM] &eWelcome to the fireworks show!",
                "&8[EM] &eYou've left the party!");
    }
}
