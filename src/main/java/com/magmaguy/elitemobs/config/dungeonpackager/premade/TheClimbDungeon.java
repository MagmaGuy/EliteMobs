package com.magmaguy.elitemobs.config.dungeonpackager.premade;

import com.magmaguy.elitemobs.config.dungeonpackager.DungeonPackagerConfigFields;
import com.magmaguy.elitemobs.utils.DiscordLinks;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TheClimbDungeon extends DungeonPackagerConfigFields {
    public TheClimbDungeon() {
        super("the_climb_dungeon",
                true,
                "&2[lvl 010] &3The Climb Dungeon",
                new ArrayList<>(List.of("&fThe perfect starter instanced dungeon!",
                        "&6Credits: MagmaGuy, Frostcone, 69OzCanOfBepis, Realm of Lotheridon")),
                DiscordLinks.freeMinidungeons,
                DungeonSizeCategory.DUNGEON,
                "em_id_the_climb",
                World.Environment.NORMAL,
                true,
                "em_id_the_climb,371.5,34,350.5,-70.0,-10.0",
                "em_id_the_climb,379.5,36,352.5,-70,-10",
                0,
                "Difficulty: &45-man hard content!",
                "&bYou've been asked to investigate the path to the caves. Climb your way to the top!",
                "&bYou have left The Climb!",
                List.of("filename=the_climb_undead_beastmaster.yml",
                        "filename=the_climb_skeletal_shadow.yml",
                        "filename=the_climb_bone_champion.yml",
                        "filename=the_climb_undead_guardian.yml"),
                "em_id_the_climb",
                10,
                false);
        setDifficulties(List.of(
                Map.of("name", "normal", "levelSync", 15, "id", 0),
                Map.of("name", "hard", "levelSync", 10, "id", 1),
                Map.of("name", "mythic", "levelSync", 5, "id", 2)));
    }
}
