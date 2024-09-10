package com.magmaguy.elitemobs.config.contentpackages.premade;

import com.magmaguy.elitemobs.config.contentpackages.ContentPackagesConfigFields;
import com.magmaguy.elitemobs.utils.DiscordLinks;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TheCaveSanctum extends ContentPackagesConfigFields {
    public TheCaveSanctum() {
        super("the_cave_sanctum",
                true,
                "&2[lvl 010] &3The Cave Sanctum",
                new ArrayList<>(List.of("&fThe perfect starter instanced sanctum!",
                        "&6Credits: MagmaGuy, Frostcone, 69OzCanOfBepis, Realm of Lotheridon")),
                DiscordLinks.freeMinidungeons,
                DungeonSizeCategory.SANCTUM,
                "em_id_the_cave",
                World.Environment.NORMAL,
                true,
                "em_id_the_cave,0.5,65,0.5,70,0",
                "em_id_the_cave,-18.5,58,-17.5,90.0,30.0",
                0,
                "Difficulty: &45-man hard content!",
                "&bYou've been asked to clear the path to the underground. Deal with the thing in the cave!",
                "&bYou have left The Cave!",
                List.of("filename=the_cave_boiler_p1.yml"),
                "em_id_the_cave",
                15,
                false);
        setDifficulties(List.of(
                Map.of("name", "normal", "levelSync", 20, "id", 0),
                Map.of("name", "hard", "levelSync", 15, "id", 1),
                Map.of("name", "mythic", "levelSync", 10, "id", 2)));
    }
}
