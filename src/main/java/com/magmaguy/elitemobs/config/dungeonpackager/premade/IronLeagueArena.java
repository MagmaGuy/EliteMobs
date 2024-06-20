package com.magmaguy.elitemobs.config.dungeonpackager.premade;

import com.magmaguy.elitemobs.config.dungeonpackager.DungeonPackagerConfigFields;
import com.magmaguy.elitemobs.utils.DiscordLinks;
import org.bukkit.World;

import java.util.List;

public class IronLeagueArena extends DungeonPackagerConfigFields {
    public IronLeagueArena() {
        super("iron_league_arena",
                true,
                "&2[lvl 050-100] &fThe Iron League Arena",
                List.of("&fFace the Iron League Arena!"),
                DiscordLinks.freeMinidungeons,
                DungeonSizeCategory.ARENA,
                "em_iron_league_arena",
                World.Environment.NORMAL,
                true,
                "em_iron_league_arena,46,-42,38,-110,-24",
                0,
                "Difficulty: &chard\n" +
                        "Face 50 waves of the arena, from\n" +
                        "level 50 to level 100!",
                "&fAre you prepared to face the arena?",
                "&fNow leaving the iron league arena!",
                "iron_league_arena",
                false);
    }
}
