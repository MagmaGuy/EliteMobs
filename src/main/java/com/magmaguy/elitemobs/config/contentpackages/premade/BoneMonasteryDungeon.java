package com.magmaguy.elitemobs.config.contentpackages.premade;

import com.magmaguy.elitemobs.config.contentpackages.ContentPackagesConfigFields;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BoneMonasteryDungeon extends ContentPackagesConfigFields {
    public BoneMonasteryDungeon() {
        super("bone_monastery_dungeon",
                true,
                "&2[lvl 085] &cThe Bone Monastery",
                new ArrayList<>(List.of("&fSpooky skeletons await.",
                        "&6Credits: MagmaGuy, Matevagyok, Frostcone, 69OzCanOfBepis")),
                "https://nightbreak.io/plugin/elitemobs/#bone-monastery",
                DungeonSizeCategory.DUNGEON,
                "em_id_bone_monastery",
                World.Environment.NORMAL,
                true,
                "em_id_bone_monastery,-249.5,18.0,109.5,-174.499999,18.499996",
                "em_id_bone_monastery,-171.5,5.0,54.5,-174.499999,18.499996",
                0,
                "Difficulty: &45-man content!",
                "&bWelcome to the bone monastery! Mind the skeletons.",
                "&bYou have left the bone monastery!",
                List.of("filename=bone_monastery_bone_saint_himiko_p1.yml"),
                "the_bone_monastery",
                85,
                false);
        setDifficulties(List.of(
                Map.of("name", "normal", "levelSync", 90, "id", 0),
                Map.of("name", "hard", "levelSync", 85, "id", 1),
                Map.of("name", "mythic", "levelSync", 80, "id", 2)));
        setSetupMenuDescription(List.of(
                "&2A Dungeon for players around level 85!"));
    }
}
