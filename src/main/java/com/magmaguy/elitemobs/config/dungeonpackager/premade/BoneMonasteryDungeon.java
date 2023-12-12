package com.magmaguy.elitemobs.config.dungeonpackager.premade;

import com.magmaguy.elitemobs.config.dungeonpackager.DungeonPackagerConfigFields;
import com.magmaguy.elitemobs.utils.DiscordLinks;
import org.bukkit.World;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class BoneMonasteryDungeon extends DungeonPackagerConfigFields {
    public BoneMonasteryDungeon() {
        super("bone_monastery_dungeon",
                false,
                "&2[lvl 085]&cThe Bone Monastery",
                Arrays.asList("&fSpooky skeletons await.",
                        "&6Credits: MagmaGuy, Matevagyok, Frostcone, 69OzCanOfBepis"),
                DiscordLinks.premiumMinidungeons,
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
                85);
        setDifficulties(List.of(
                Map.of("name", "normal", "levelSync", 90, "id", 0),
                Map.of("name", "hard", "levelSync", 85, "id", 1),
                Map.of("name", "mythic", "levelSync", 80, "id", 2)));
    }
}
