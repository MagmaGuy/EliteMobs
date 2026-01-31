package com.magmaguy.elitemobs.config.contentpackages.premade;

import com.magmaguy.elitemobs.config.contentpackages.ContentPackagesConfigFields;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TheHallowedHauntDynamicDungeon extends ContentPackagesConfigFields {
    public TheHallowedHauntDynamicDungeon() {
        super("the_hallowed_haunt_dynamic_dungeon",
                true,
                "&2[Dynamic] &3The Hallowed Haunt Dungeon",
                new ArrayList<>(List.of("&fThe spookiest manor in all of MineCraft!",
                        "&7Choose your challenge level before entering!",
                        "&6Credits: Dali_, MagmaGuy, FrostCone")),
                "https://nightbreak.io/plugin/elitemobs/#the-hallowed-haunt",
                DungeonSizeCategory.DUNGEON,
                "em_id_the_hallowed_haunt",
                World.Environment.NORMAL,
                true,
                "em_id_the_hallowed_haunt,-0.5,72,146.5,170,0",
                "em_id_the_hallowed_haunt,-4.5,72,131.5,162,0",
                1,
                "Difficulty: &4Dynamic 5-man content!",
                "&bBeware the horror the lurks in the manor!",
                "&bYou have left The Hallowed Haunt!",
                List.of("filename=em_the_hallowed_haunt_miniboss_ooze.yml",
                        "filename=em_the_hallowed_haunt_miniboss_phantom.yml",
                        "filename=em_the_hallowed_haunt_final_boss_central_heater.yml"),
                "em_id_the_hallowed_haunt",
                -1, // -1 indicates dynamic content level (set by player in menu)
                false);

        // Override content type to DYNAMIC_DUNGEON
        this.contentType = ContentType.DYNAMIC_DUNGEON;

        // Relative levelSync: +5 means player's selected level + 5, -3 means selected level - 3
        // normal: easier (+5 means higher gear can be used), mythic: harder (-0 means gear must match level)
        setDifficulties(List.of(
                Map.of("name", "normal", "levelSync", "+5", "id", 0),
                Map.of("name", "hard", "levelSync", "0", "id", 1),
                Map.of("name", "mythic", "levelSync", "-5", "id", 2)));
        setSetupMenuDescription(List.of(
                "&2A Dynamic Dungeon where you choose the level!",
                "&2Select from levels based on your guild rank!"));
        setDungeonLockoutMinutes(1440); // 24 hour lockout
        setNightbreakSlug("the-hallowed-haunt");
    }
}
