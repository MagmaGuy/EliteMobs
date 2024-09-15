package com.magmaguy.elitemobs.config.contentpackages.premade;

import com.magmaguy.elitemobs.config.contentpackages.ContentPackagesConfigFields;
import org.bukkit.World;

import java.util.List;

public class CatacombsLair extends ContentPackagesConfigFields {
    public CatacombsLair() {
        super("catacombs_lair",
                true,
                "&2[lvl 010] &8The Catacombs",
                List.of("&fA great starter lair for players!",
                        "&6Credits: Realm of Lotheridon, MagmaGuy, Dali, Frost"),
                "https://nightbreak.io/plugin/elitemobs/#the-catacombs",
                DungeonSizeCategory.LAIR,
                "em_the_catacombs",
                World.Environment.NORMAL,
                true,
                "em_the_catacombs,41.5,65.0,82.5,176,0",
                0,
                "Difficulty: &cHard\n" +
                        "$bossCount bosses, from tier $lowestTier to $highestTier\n" +
                        "&2A great Lair challenge for groups of beginners!",
                "&8[EM] &8Now entering the Catacombs. Be careful with what dwells below...",
                "&8[EM] &8You have left the Catacombs. Was it worth it?",
                "the_catacombs",
                false);
        setSetupMenuDescription(List.of(
                "&2A level 10 lair for new players!"));
    }
}