package com.magmaguy.elitemobs.config.dungeonpackager.premade;

import com.magmaguy.elitemobs.config.dungeonpackager.DungeonPackagerConfigFields;
import com.magmaguy.elitemobs.utils.DiscordLinks;
import org.bukkit.World;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class TheNetherBellSanctum extends DungeonPackagerConfigFields {
    public TheNetherBellSanctum() {
        super("the_nether_bell_sanctum",
                true,
                "&2[lvl 055] &3The Nether Bell Sanctum",
                Arrays.asList("&fVenture into the deepest part of the Nether!",
                        "&6Credits: Dali_, MagmaGuy, Frostcone"),
                DiscordLinks.freeMinidungeons,
                DungeonSizeCategory.SANCTUM,
                "em_id_the_nether_bell",
                World.Environment.NORMAL,
                true,
                "em_id_the_nether_bell,231.5,87,-219.5,44,0",
                "em_id_the_nether_bell,184.5,69,-192.5,43,0",
                0,
                "Difficulty: &45-man hard content!",
                "&bNo being should be this deep in the Nether...",
                "&bYou have left The Nether Bell!",
                List.of("filename=em_id_the_nether_bell_boss_void_bell_p1.yml"),
                "em_id_the_nether_bell",
                55,
                false);
        setDifficulties(List.of(
                Map.of("name", "normal", "levelSync", 57, "id", 0),
                Map.of("name", "hard", "levelSync", 55, "id", 1),
                Map.of("name", "mythic", "levelSync", 53, "id", 2)));
    }
}
