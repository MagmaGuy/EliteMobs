package com.magmaguy.elitemobs.config.dungeonpackager.premade;

import com.magmaguy.elitemobs.config.dungeonpackager.DungeonPackagerConfigFields;
import com.magmaguy.elitemobs.utils.DiscordLinks;
import org.bukkit.util.Vector;

import java.util.Arrays;

public class CatacombsLair extends DungeonPackagerConfigFields {
    public CatacombsLair() {
        super("catacombs_lair",
                false,
                "&fThe Catacombs",
                DungeonLocationType.SCHEMATIC,
                Arrays.asList("&fThe best starter dungeon for players!",
                        "&6Credits: Realm of Lotheridon"),
                Arrays.asList(
                        "catacombs_tier_5_evoker_guard_2.yml:2.5,0.5,13.5",
                        "catacombs_tier_5_illusioner_guard_1.yml:-2.5,2.5,16.5",
                        "catacombs_tier_15_boss.yml:0.5,-7.5,45.5",
                        "catacombs_tier_10_miniboss.yml:-8.5,-15.5,36.5"),
                Arrays.asList(),
                DiscordLinks.premiumMinidungeons,
                DungeonSizeCategory.LAIR,
                null,
                "em_catacombs.schem",
                null,
                true,
                new Vector(13, -16, 0),
                new Vector(-18, 17, 52),
                new Vector(0, 0, 0),
                null,
                null,
                2,
                "Difficulty: &aEasy\n" +
                        "$bossCount bosses, from tier $lowestTier to $highestTier\n" +
                        "&2The best minidungeon for beginners!",
                "&8[EM] &8Now entering the Catacombs. Be careful with what dwells below...",
                "&8[EM] &8You have left the Catacombs. Was it worth it?");
    }
}
