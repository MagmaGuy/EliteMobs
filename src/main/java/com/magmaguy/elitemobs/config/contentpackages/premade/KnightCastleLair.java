package com.magmaguy.elitemobs.config.contentpackages.premade;

import com.magmaguy.elitemobs.config.contentpackages.ContentPackagesConfigFields;
import org.bukkit.World;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KnightCastleLair extends ContentPackagesConfigFields {
    public KnightCastleLair() {
        super("knights_castle_lair",
                true,
                "&fThe Knight's Castle",
                List.of("&fChallenge the knights of the castle!"),
                "https://nightbreak.io/plugin/elitemobs/#the-knights-castle",
                DungeonSizeCategory.LAIR,
                "em_knight_castle",
                World.Environment.NORMAL,
                true,
                "em_knight_castle,-20.5,6.2,55.5,-120,0",
                0,
                "Difficulty: &chard\n" +
                        "$bossCount level $lowestTier Big Boss!\n" +
                        "&6Face an honorable duel!",
                "&fAssault the castle!",
                "&fNow leaving the castle!",
                "the_knight_castle",
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
    }
}
