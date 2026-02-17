package com.magmaguy.elitemobs.config.contentpackages.premade;

import com.magmaguy.elitemobs.config.contentpackages.ContentPackagesConfigFields;
import com.magmaguy.elitemobs.utils.DiscordLinks;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TheNetherWastes extends ContentPackagesConfigFields {
    public TheNetherWastes() {
        super("the_nether_wastes_dungeon",
                true,
                "&3The Nether Wastes Dungeon",
                new ArrayList<>(List.of("&fAn unexplored part of the Nether.",
                        "&6Credits: MagmaGuy, Frostcone, Dali_")),
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
                -1,
                false);
        setContentType(ContentType.DYNAMIC_DUNGEON);
        setDifficulties(List.of(
                Map.of("name", "normal", "levelSync", "+5", "id", 0),
                Map.of("name", "hard", "levelSync", "+0", "id", 1),
                Map.of("name", "mythic", "levelSync", "-5", "id", 2)));
        setDungeonLockoutMinutes(1440);
        setNightbreakSlug("story-mode-dungeons");
    }
}
