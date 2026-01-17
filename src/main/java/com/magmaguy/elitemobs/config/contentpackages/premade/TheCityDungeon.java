package com.magmaguy.elitemobs.config.contentpackages.premade;

import com.magmaguy.elitemobs.config.contentpackages.ContentPackagesConfigFields;
import com.magmaguy.elitemobs.utils.DiscordLinks;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TheCityDungeon extends ContentPackagesConfigFields {
    public TheCityDungeon() {
        super("the_city_dungeon",
                true,
                "&3The City Dungeon",
                new ArrayList<>(List.of("&fThe perfect intermediate instanced sanctum!",
                        "&6Credits: Dali_, MagmaGuy, Frostcone")),
                DiscordLinks.freeMinidungeons,
                DungeonSizeCategory.DUNGEON,
                "em_id_the_city",
                World.Environment.NORMAL,
                true,
                "em_id_the_city,-87.5,119,-13.5,-90,0",
                "em_id_the_city,-60.5,122,-12.5,-67,0",
                0,
                "Difficulty: &45-man hard content!",
                "&bBefore being able to enter the city you must deal with the guardian on the bridge!",
                "&bYou have left The City!",
                List.of("filename=em_id_the_city_mini_boss_one.yml",
                        "filename=em_id_the_city_mini_boss_two.yml",
                        "filename=em_id_the_city_mini_boss_three.yml",
                        "filename=em_id_the_city_royal_guard_p1.yml"),
                "em_id_the_city",
                -1,
                false);
        setContentType(ContentType.DYNAMIC_DUNGEON);
        setDifficulties(List.of(
                Map.of("name", "normal", "levelSync", "+5", "id", 0),
                Map.of("name", "hard", "levelSync", "+0", "id", 1),
                Map.of("name", "mythic", "levelSync", "-5", "id", 2)));
        setDungeonLockoutMinutes(1440);
    }
}
