package com.magmaguy.elitemobs.config.dungeonpackager.premade;

import com.magmaguy.elitemobs.config.dungeonpackager.DungeonPackagerConfigFields;
import com.magmaguy.elitemobs.utils.DiscordLinks;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.List;

public class FireworksLair extends DungeonPackagerConfigFields {
    public FireworksLair() {
        super("fireworks_lair",
                true,
                "&2[lvl 020] &aThe Fireworks",
                new ArrayList<>(List.of("&fThe 2021 4th of July map!",
                        "&6Credits: MagmaGuy")),
                DiscordLinks.freeMinidungeons,
                DungeonSizeCategory.LAIR,
                "em_fireworks",
                World.Environment.NORMAL,
                true,
                "em_fireworks,29.5,66.2,-32.5,45,0",
                0,
                "Difficulty: &cHard\n" +
                        "$bossCount level $lowestTier Big Boss!\n" +
                        "&6An encounter full of explosions!",
                "&8[EM] &eWelcome to the fireworks show!",
                "&8[EM] &eYou've left the party!",
                "the_fireworks",
                true);
    }
}
