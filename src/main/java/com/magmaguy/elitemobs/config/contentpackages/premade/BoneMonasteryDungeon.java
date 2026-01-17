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
                "&cThe Bone Monastery",
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
                -1,
                false);
        setContentType(ContentType.DYNAMIC_DUNGEON);
        setDifficulties(List.of(
                Map.of("name", "normal", "levelSync", "+5", "id", 0),
                Map.of("name", "hard", "levelSync", "+0", "id", 1),
                Map.of("name", "mythic", "levelSync", "-5", "id", 2)));
        setDungeonLockoutMinutes(1440);
        setSetupMenuDescription(List.of(
                "&2A dynamic Dungeon!"));
    }
}
