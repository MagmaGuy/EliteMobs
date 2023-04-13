package com.magmaguy.elitemobs.config.dungeonpackager.premade;

import com.magmaguy.elitemobs.config.dungeonpackager.DungeonPackagerConfigFields;
import com.magmaguy.elitemobs.utils.DiscordLinks;
import org.bukkit.World;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class TheCaveSanctum extends DungeonPackagerConfigFields {
    public TheCaveSanctum() {
        super("the_cave_sanctum",
                false,
                "&2[lvl 010] &3The Cave Dungeon",
                Arrays.asList("&fThe perfect starter instanced sanctum!",
                        "&6Credits: MagmaGuy, Frostcone, 69OzCanOfBepis, Realm of Lotheridon"),
                DiscordLinks.freeMinidungeons,
                DungeonSizeCategory.SANCTUM,
                "em_id_the_cave",
                World.Environment.NORMAL,
                true,
                "em_id_the_cave,371.5,34,350.5,70,0",
                "em_id_the_cave,379.5,36,352.5,90.0,30.0",
                0,
                "Difficulty: &45-man hard content!",
                "&bYou've been asked to clear the path to the underground. Deal with the thing in the cave!",
                "&bYou have left The Cave!",
                List.of("filename=the_cave_thing_in_the_cave_p1.yml"),
                "em_id_the_cave",
                10);
        setDifficulties(List.of(
                Map.of("name", "normal", "levelSync", 12, "id", 0),
                Map.of("name", "hard", "levelSync", 10, "id", 1),
                Map.of("name", "mythic", "levelSync", 8, "id", 2)));
    }
}