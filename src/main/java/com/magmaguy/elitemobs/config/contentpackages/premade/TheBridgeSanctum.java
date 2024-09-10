package com.magmaguy.elitemobs.config.contentpackages.premade;

import com.magmaguy.elitemobs.config.contentpackages.ContentPackagesConfigFields;
import com.magmaguy.elitemobs.utils.DiscordLinks;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TheBridgeSanctum extends ContentPackagesConfigFields {
    public TheBridgeSanctum() {
        super("the_bridge_sanctum",
                true,
                "&2[lvl 025] &3The Bridge Sanctum",
                new ArrayList<>(List.of("&fThe perfect intermediate instanced sanctum!",
                        "&6Credits: Dali_, MagmaGuy, Frostcone")),
                DiscordLinks.freeMinidungeons,
                DungeonSizeCategory.SANCTUM,
                "em_id_the_bridge",
                World.Environment.NORMAL,
                true,
                "em_id_the_bridge,-255.5,100,-14.5,-90.0,0.0",
                "em_id_the_bridge,-216.5,103,-14.5,-90.0,0.0",
                0,
                "Difficulty: &45-man hard content!",
                "&bBefore being able to enter the city you must deal with the guardian on the bridge!",
                "&bYou have left The Bridge!",
                List.of("filename=the_bridge_ancient_guardian_p1.yml"),
                "em_id_the_bridge",
                25,
                false);
        setDifficulties(List.of(
                Map.of("name", "normal", "levelSync", 30, "id", 0),
                Map.of("name", "hard", "levelSync", 25, "id", 1),
                Map.of("name", "mythic", "levelSync", 20, "id", 2)));
    }
}
