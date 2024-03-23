package com.magmaguy.elitemobs.config.dungeonpackager.premade;

import com.magmaguy.elitemobs.config.dungeonpackager.DungeonPackagerConfigFields;
import com.magmaguy.elitemobs.utils.DiscordLinks;
import org.bukkit.World;

import java.util.Arrays;
import java.util.List;

public class KnightCastleLair extends DungeonPackagerConfigFields {
    public KnightCastleLair() {
        super("knights_castle_lair",
                false,
                "&2[lvl 095] &fThe Knight's Castle",
                List.of("&fChallenge the knights of the castle!"),
                DiscordLinks.freeMinidungeons,
                DungeonSizeCategory.LAIR,
                "em_knight_castle",
                World.Environment.NORMAL,
                true,
                "em_knight_castle,-24.5,5.0,54.5,-117,0",
                0,
                "Difficulty: &chard\n" +
                        "$bossCount level $lowestTier Big Boss!\n" +
                        "&6Face an honorable duel!",
                "&fAssault the castle!",
                "&fNow leaving the castle!",
                "the_knight_castle"
        );
    }
}
