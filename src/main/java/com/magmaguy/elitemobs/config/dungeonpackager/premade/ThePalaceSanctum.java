package com.magmaguy.elitemobs.config.dungeonpackager.premade;

import com.magmaguy.elitemobs.config.dungeonpackager.DungeonPackagerConfigFields;
import com.magmaguy.elitemobs.utils.DiscordLinks;
import org.bukkit.World;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class ThePalaceSanctum extends DungeonPackagerConfigFields {
    public ThePalaceSanctum(){
        super("the_palace_sanctum",
                false,
                "&2[lvl 030] &3The Palace Sanctum",
                Arrays.asList("&fThe perfect intermediate instanced sanctum!",
                        "&6Credits: Dali_, MagmaGuy, Frostcone"),
                DiscordLinks.freeMinidungeons,
                DungeonSizeCategory.SANCTUM,
                "em_id_the_palace",
                World.Environment.NORMAL,
                true,
                "em_id_the_palace,208.5,144,-101.5,-90.0,0.0",
                "em_id_the_palace,238.5,144,-100.5,0.0,0.0",
                0,
                "Difficulty: &45-man hard content!",
                "&bBefore being able to enter the city you must deal with the guardian on the bridge!",
                "&bYou have left The Palace!",
                List.of("the_palace_old_stone_king_p1.yml"),
                "em_id_the_palace",
                35);
        setDifficulties(List.of(
                Map.of("name", "normal", "levelSync", 40, "id", 0),
                Map.of("name", "hard", "levelSync", 35, "id", 1),
                Map.of("name", "mythic", "levelSync", 30, "id", 2)));
    }
}
