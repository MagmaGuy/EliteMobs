package com.magmaguy.elitemobs.config.contentpackages.premade;

import com.magmaguy.elitemobs.config.contentpackages.ContentPackagesConfigFields;
import com.magmaguy.elitemobs.utils.DiscordLinks;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TheDeepMinesDungeon extends ContentPackagesConfigFields {
    public TheDeepMinesDungeon() {
        super("the_deep_mines_dungeon",
                true,
                "&3The Deep Mines Dungeon",
                new ArrayList<>(List.of("&fBelow lies the deepest mine ever dug.",
                        "&6Credits: Dali, MagmaGuy, FrostCone")),
                DiscordLinks.freeMinidungeons,
                DungeonSizeCategory.SANCTUM,
                "em_id_the_deep_mines",
                World.Environment.NORMAL,
                true,
                "em_id_the_deep_mines,-205.5,154,-85.5,0,0",
                "em_id_the_deep_mines,-207.5,154,-68.5,0,0",
                0,
                "Difficulty: &45-man hard content!",
                "&bVenture far down and find out how deep the deep mines are.",
                "&bYou have left The Deep Mines!",
                List.of("filename=em_id_the_deep_mines_tnt_box.yml",
                        "filename=em_id_the_deep_mines_boss_the_pursuer_p1.yml"),
                "em_id_the_deep_mines",
                -1,
                false);
        setContentType(ContentType.DYNAMIC_DUNGEON);
        setDifficulties(List.of(
                Map.of("name", "normal", "levelSync", "+5", "id", 0),
                Map.of("name", "hard", "levelSync", "+0", "id", 1),
                Map.of("name", "mythic", "levelSync", "-5", "id", 2)));
        setDungeonLockoutMinutes(1440);
        setNightbreakSlug("story-mode-dungeons");
    }
}
