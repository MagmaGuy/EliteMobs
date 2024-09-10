package com.magmaguy.elitemobs.config.contentpackages.premade;

import com.magmaguy.elitemobs.config.contentpackages.ContentPackagesConfigFields;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.List;

public class FireworksLair extends ContentPackagesConfigFields {
    public FireworksLair() {
        super("fireworks_lair",
                true,
                "&2[lvl 020] &aThe Fireworks",
                new ArrayList<>(List.of("&fThe 2021 4th of July map!",
                        "&6Credits: MagmaGuy")),
                "https://nightbreak.io/plugin/elitemobs/#fireworks",
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
        setSetupMenuDescription(List.of(
                "&2A Lair for players around level 20!"));
    }
}
