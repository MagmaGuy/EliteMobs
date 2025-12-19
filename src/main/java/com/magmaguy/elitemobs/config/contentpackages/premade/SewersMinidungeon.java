package com.magmaguy.elitemobs.config.contentpackages.premade;

import com.magmaguy.elitemobs.config.contentpackages.ContentPackagesConfigFields;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SewersMinidungeon extends ContentPackagesConfigFields {
    public SewersMinidungeon() {
        super("sewers_minidungeon",
                true,
                "&2[Dynamic] &8The Sewers",
                new ArrayList<>(List.of("&fThe biggest minidungeon ever made!",
                        "&6Credits: MagmaGuy & 69OzCanOfBepis")),
                "https://nightbreak.io/plugin/elitemobs/#the-sewers",
                DungeonSizeCategory.MINIDUNGEON,
                "em_sewer_maze",
                World.Environment.NORMAL,
                true,
                "em_sewer_maze,-24.0,168.2,-174.0,90,0",
                "em_sewer_maze,-29.9,168.2,-173.9,90,0",
                0,
                "Difficulty: &6Medium\n" +
                        "$bossCount bosses, from level $lowestTier to $highestTier\n" +
                        "&6A complex dungeon maze with a challenging sections!",
                "&8[EM] &5This place looks abandoned... &dYet something lurks in these sewers!",
                "&8[EM] &5You managed to stay above water. &dMaybe next time you'll drown.",
                List.of("filename=sewer_tier_70_boss.yml"),
                "the_sewers",
                -1,
                false);
        setSetupMenuDescription(List.of(
                "&2A Minidungeon for players around level 25!",
                "&2and custom powers, all for free!"
        ));
        setDifficulties(List.of(
                Map.of("name", "normal", "levelSync", "+5", "id", 0),
                Map.of("name", "hard", "levelSync", "0", "id", 1),
                Map.of("name", "mythic", "levelSync", "-5", "id", 2)));
        this.contentType = ContentType.DYNAMIC_DUNGEON;
        setDungeonLockoutMinutes(1440);
    }
}
