package com.magmaguy.elitemobs.config.contentpackages.premade;

import com.magmaguy.elitemobs.config.contentpackages.ContentPackagesConfigFields;
import org.bukkit.World;

import java.util.List;
import java.util.Map;

public class RuinsLair extends ContentPackagesConfigFields {
    public RuinsLair() {
        super("the_ruins",
                true,
                "&2[Dynamic] &6The Ruins",
                List.of("&fA fight against a myth from",
                        "&fNorse mythology, be prepared for a smiting!",
                        "&6Credits: 69OzCanOfBepis, MagmaGuy, Dali, Frost"),
                "https://nightbreak.io/plugin/elitemobs/#the-ruins",
                DungeonSizeCategory.LAIR,
                "em_the_ruins",
                World.Environment.NORMAL,
                true,
                "em_the_ruins,-63.5,190.0,111.5,-132,0",
                0,
                "Difficulty: &cHard\n" +
                        "$bossCount level $lowestTier Big Boss!\n" +
                        "&cA tough fight against a Norse god!",
                "&8[EM] &3Those who challenge the myths must be prepared for their downfall!",
                "&8[EM] &3You now know what it takes to make a legend!",
                "em_the_ruins",
                false);
        setContentType(ContentType.DYNAMIC_DUNGEON);
        setContentLevel(-1);
        setDifficulties(List.of(
                Map.of("name", "normal", "levelSync", "+5", "id", 0),
                Map.of("name", "hard", "levelSync", "+0", "id", 1),
                Map.of("name", "mythic", "levelSync", "-5", "id", 2)));
        setDungeonLockoutMinutes(1440);
        setSetupMenuDescription(List.of(
                "&2A dynamic Lair!"));
        setNightbreakSlug("the-ruins");
    }
}