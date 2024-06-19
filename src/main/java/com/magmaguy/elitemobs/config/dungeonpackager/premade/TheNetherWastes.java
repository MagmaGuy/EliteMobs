package com.magmaguy.elitemobs.config.dungeonpackager.premade;

import com.magmaguy.elitemobs.config.dungeonpackager.DungeonPackagerConfigFields;
import com.magmaguy.elitemobs.utils.DiscordLinks;
import org.bukkit.World;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class TheNetherWastes extends DungeonPackagerConfigFields {
    public TheNetherWastes() {
        super("the_nether_wastes_dungeon",
                false,
                "&2[lvl 050] &3The Nether Wastes Dungeon",
                Arrays.asList("&fAn unexplored part of the Nether.",
                        "&6Credits: MagmaGuy, Frostcone, Dali_"),
                DiscordLinks.freeMinidungeons,
                DungeonSizeCategory.DUNGEON,
                "em_id_the_nether_wastes",
                World.Environment.NORMAL,
                true,
                "em_id_the_nether_wastes,38.5,84,64.5,-155,0",
                "em_id_the_nether_wastes,41.5,82.5,55.5,-167,0",
                0,
                "Difficulty: &45-man hard content!",
                "&bTraverse the wastes and see what you can find.",
                "&bYou have left The Nether Wastes!",
                List.of("filename=em_id_the_nether_wastes_miniboss_5_shroud_p1.yml"),
                "em_id_the_nether_wastes",
                50);
        setDifficulties(List.of(
                Map.of("name", "normal", "levelSync", 52, "id", 0),
                Map.of("name", "hard", "levelSync", 50, "id", 1),
                Map.of("name", "mythic", "levelSync", 48, "id", 2)));
    }
}
