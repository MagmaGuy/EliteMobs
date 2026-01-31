package com.magmaguy.elitemobs.config.contentpackages.premade;

import com.magmaguy.elitemobs.config.contentpackages.ContentPackagesConfigFields;
import org.bukkit.World;

import java.util.List;
import java.util.Map;

public class TheColosseum extends ContentPackagesConfigFields {
    public TheColosseum() {
        super("the_colosseum_lair",
                true,
                "&2[Dynamic] &8The Colosseum",
                List.of("&fA colosseum!", "&6Credits: MagmaGuy, Dali, Frost"),
                "https://nightbreak.io/plugin/elitemobs/#the-colosseum",
                DungeonSizeCategory.LAIR,
                "em_the_colosseum",
                World.Environment.NORMAL,
                true,
                "em_the_colosseum,-83.5,66.0,251.5,-72,0",
                0,
                "Difficulty: &cHard\n" +
                        "$bossCount bosses, from tier $lowestTier to $highestTier\n" +
                        "&2A great Lair!",
                "&8[EM] &8Now entering the Colosseum. Do you dare challenge the champion...",
                "&8[EM] &8You have left the Colosseum.",
                "the_colosseum",
                false);
        setContentType(ContentType.DYNAMIC_DUNGEON);
        setContentLevel(-1);
        setDifficulties(List.of(
                Map.of("name", "normal", "levelSync", "+5", "id", 0),
                Map.of("name", "hard", "levelSync", "+0", "id", 1),
                Map.of("name", "mythic", "levelSync", "-5", "id", 2)));
        setDungeonLockoutMinutes(1440);
        setSetupMenuDescription(List.of(
                "&2A dynamic Lair!"
        ));
        setNightbreakSlug("the-colosseum-remake-soon");
    }
}