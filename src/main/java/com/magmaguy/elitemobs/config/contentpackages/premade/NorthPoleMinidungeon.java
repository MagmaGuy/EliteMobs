package com.magmaguy.elitemobs.config.contentpackages.premade;

import com.magmaguy.elitemobs.config.contentpackages.ContentPackagesConfigFields;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NorthPoleMinidungeon extends ContentPackagesConfigFields {
    public NorthPoleMinidungeon() {
        super("north_pole_minidungeon",
                true,
                "&9The North Pole",
                new ArrayList<>(List.of("&fThe Christmas minidungeon!",
                        "&6Credits: MagmaGuy & 69OzCanOfBepis")),
                "https://nightbreak.io/plugin/elitemobs/#north-pole",
                DungeonSizeCategory.MINIDUNGEON,
                "em_north_pole",
                World.Environment.NORMAL,
                true,
                "em_north_pole,-264.5,43.2,-503.5,47,0",
                0,
                "Difficulty: &6Medium\n" +
                        "$bossCount bosses, from level $lowestTier to $highestTier\n" +
                        "&6Christmas in a snow globe!",
                "&8[EM] &7You have reached the North Pole! &fHave you been naughty this year?",
                "&8[EM] &7Come back and visit. &fThere are plenty of sweets and treats for next time!",
                "the_north_pole",
                false);
        setContentType(ContentType.DYNAMIC_DUNGEON);
        setContentLevel(-1);
        setDifficulties(List.of(
                Map.of("name", "normal", "levelSync", "+5", "id", 0),
                Map.of("name", "hard", "levelSync", "+0", "id", 1),
                Map.of("name", "mythic", "levelSync", "-5", "id", 2)));
        setDungeonLockoutMinutes(1440);
        setSetupMenuDescription(List.of(
                "&2A dynamic Christmas-themed Minidungeon!"));
    }
}
