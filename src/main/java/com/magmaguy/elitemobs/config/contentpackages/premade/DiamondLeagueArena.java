package com.magmaguy.elitemobs.config.contentpackages.premade;

import com.magmaguy.elitemobs.config.contentpackages.ContentPackagesConfigFields;
import org.bukkit.World;

import java.util.List;

public class DiamondLeagueArena extends ContentPackagesConfigFields {
    public DiamondLeagueArena() {
        super("diamond_league_arena",
                true,
                "&5[lvl 100-150] &fThe Diamond League Arena",
                List.of("&fFace the Diamond League Arena!"),
                "https://nightbreak.io/plugin/elitemobs/#arena-diamond-league-arena",
                DungeonSizeCategory.ARENA,
                "em_diamond_arena",
                World.Environment.NORMAL,
                true,
                "em_diamond_arena,4.5,78,-28.5,0,0",
                0,
                "Difficulty: &chard\n" +
                        "Face 50 waves of the arena, from\n" +
                        "level 100 to level 150!",
                "&fAre you prepared to face the arena?",
                "&fNow leaving the diamond league arena!",
                "diamond_league_arena",
                false);
        setSetupMenuDescription(List.of(
                "&2An arena for players between levels 100-150!"));
    }
}
