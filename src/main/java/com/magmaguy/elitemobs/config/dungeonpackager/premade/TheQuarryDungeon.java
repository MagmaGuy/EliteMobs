package com.magmaguy.elitemobs.config.dungeonpackager.premade;

import com.magmaguy.elitemobs.config.dungeonpackager.DungeonPackagerConfigFields;
import com.magmaguy.elitemobs.utils.DiscordLinks;
import org.bukkit.World;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class TheQuarryDungeon extends DungeonPackagerConfigFields {
    public TheQuarryDungeon() {
        super("the_quarry_dungeon",
                true,
                "&2[lvl 040] &3The Quarry Dungeon",
                Arrays.asList("&fAn ancient dwarven quarry deep underground.",
                        "&6Credits: Dali_, MagmaGuy, Frostcone"),
                DiscordLinks.freeMinidungeons,
                DungeonSizeCategory.DUNGEON,
                "em_id_the_quarry",
                World.Environment.NORMAL,
                true,
                "em_id_the_quarry,230.5,144,102.5,-67,0",
                "em_id_the_quarry,245.5,138,109.5,-90,30",
                0,
                "Difficulty: &45-man hard content!",
                "&bGo down into the quarry and get that lift working.",
                "&bYou have left The Quarry!",
                List.of("filename=em_id_the_quarry_quarry_smith.yml",
                        "filename=em_id_the_quarry_royal_wizard_three.yml",
                        "filename=LiftStateFinishDungeon.yml"),
                "em_id_the_quarry",
                40,
                false);
        setDifficulties(List.of(
                Map.of("name", "normal", "levelSync", 42, "id", 0),
                Map.of("name", "hard", "levelSync", 40, "id", 1),
                Map.of("name", "mythic", "levelSync", 37, "id", 2)));
    }
}
