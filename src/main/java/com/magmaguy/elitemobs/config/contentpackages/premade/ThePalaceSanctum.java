package com.magmaguy.elitemobs.config.contentpackages.premade;

import com.magmaguy.elitemobs.config.contentpackages.ContentPackagesConfigFields;
import com.magmaguy.elitemobs.utils.DiscordLinks;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ThePalaceSanctum extends ContentPackagesConfigFields {
    public ThePalaceSanctum() {
        super("the_palace_sanctum",
                true,
                "&3The Palace Sanctum",
                new ArrayList<>(List.of("&fThe perfect intermediate instanced sanctum!",
                        "&6Credits: Dali_, MagmaGuy, Frostcone")),
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
