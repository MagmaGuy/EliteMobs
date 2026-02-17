package com.magmaguy.elitemobs.config.contentpackages.premade;

import com.magmaguy.elitemobs.config.contentpackages.ContentPackagesConfigFields;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AirshipMinidungeon extends ContentPackagesConfigFields {
    public AirshipMinidungeon() {
        super("the_airship",
                true,
                "&2[Dynamic] &6The Airship",
                new ArrayList<>(List.of("&fAn awesome airship full of undead pirates!",
                        "&6Credits: MagmaGuy, 69OzCanOfBepis",
                        "&6and Realm of Lotheridon")),
                "https://nightbreak.io/plugin/elitemobs/#the-airship",
                DungeonSizeCategory.MINIDUNGEON,
                "em_the_airship",
                World.Environment.NORMAL,
                true,
                "em_the_airship,-85.5,189.5,41.5,-144,0",
                "em_the_airship,-95.5,188.0,43.5,-144,0",
                0,
                "Difficulty: &6Medium\n" +
                        "$bossCount bosses, from level $lowestTier to $highestTier\n" +
                        "&6An awesome airship full of undead pirates\n" +
                        "&6for experienced players!",
                "&8[EM] &cEntering hostile air zone!",
                "&8[EM] &cLeaving hostile air zone!",
                List.of("filename=airship_tier_75_boss.yml"),
                "the_airship",
                -1,
                false);
        this.contentType = ContentType.DYNAMIC_DUNGEON;
        setDifficulties(List.of(
                Map.of("name", "normal", "levelSync", "+5", "id", 0),
                Map.of("name", "hard", "levelSync", "+0", "id", 1),
                Map.of("name", "mythic", "levelSync", "-5", "id", 2)));
        setDungeonLockoutMinutes(1440);
        setSetupMenuDescription(List.of(
                "&2A dynamic Minidungeon!"));
        setNightbreakSlug("the-airship");
    }
}
