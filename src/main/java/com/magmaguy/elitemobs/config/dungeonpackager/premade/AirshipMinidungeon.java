package com.magmaguy.elitemobs.config.dungeonpackager.premade;

import com.magmaguy.elitemobs.config.dungeonpackager.DungeonPackagerConfigFields;
import com.magmaguy.elitemobs.utils.DiscordLinks;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.List;

public class AirshipMinidungeon extends DungeonPackagerConfigFields {
    public AirshipMinidungeon() {
        super("airship_minidungeon",
                true,
                "&2[lvl 045-055] &6The Airship",
                new ArrayList<>(List.of("&fAn awesome airship full of undead pirates!",
                        "&6Credits: MagmaGuy, 69OzCanOfBepis",
                        "&6and Realm of Lotheridon")),
                DiscordLinks.premiumMinidungeons,
                DungeonSizeCategory.MINIDUNGEON,
                "em_the_airship",
                World.Environment.NORMAL,
                true,
                "em_the_airship,-95.5,188.0,43.5,-144,0",
                0,
                "Difficulty: &6Medium\n" +
                        "$bossCount bosses, from level $lowestTier to $highestTier\n" +
                        "&6An awesome airship full of undead pirates\n" +
                        "&6for experienced players!",
                "&8[EM] &cEntering hostile air zone!",
                "&8[EM] &cLeaving hostile air zone!",
                "the_airship",
                false);
    }
}
