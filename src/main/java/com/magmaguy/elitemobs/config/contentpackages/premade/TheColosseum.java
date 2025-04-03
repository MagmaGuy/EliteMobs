package com.magmaguy.elitemobs.config.contentpackages.premade;

import com.magmaguy.elitemobs.config.contentpackages.ContentPackagesConfigFields;
import org.bukkit.World;

import java.util.List;

public class TheColosseum extends ContentPackagesConfigFields {
    public TheColosseum() {
        super("the_colosseum_lair",
                true,
                "&2[lvl 070] &8The Colosseum",
                List.of("&fA colosseum!", "&6Credits: MagmaGuy, Dali, Frost"),
                null, // website field appears to be missing in YAML, set to null
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
        setSetupMenuDescription(List.of(
                "&2A level 70 lair for seasoned players!"
        ));
    }
}