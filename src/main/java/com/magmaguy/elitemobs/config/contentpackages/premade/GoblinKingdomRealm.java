package com.magmaguy.elitemobs.config.contentpackages.premade;

import com.magmaguy.elitemobs.config.contentpackages.ContentPackagesConfigFields;
import org.bukkit.World;

import java.util.List;
import java.util.Map;

public class GoblinKingdomRealm extends ContentPackagesConfigFields {
    public GoblinKingdomRealm() {
        super("goblin_kingdom",
                true,
                "&6The Goblin Kingdom",
                List.of("&fAn instanced goblin realm with escalating encounter mechanics.",
                        "&aEasy &7uses core attacks, &eMedium &7adds mechanics, and &cHard &7enables full kits.",
                        "&6An EliteMobs dungeon by Nightbreak Studios."),
                "https://nightbreak.io/plugin/elitemobs/#goblin-kingdom",
                DungeonSizeCategory.REALM,
                "GoblinKingdom_vf",
                World.Environment.NORMAL,
                true,
                "GoblinKingdom_vf,687.5,65.0,989.5,0,0",
                "GoblinKingdom_vf,687.5,65.0,980.5,0,0",
                0,
                "&6Three difficulty modes\n" +
                        "&aEasy &7- core mechanics\n" +
                        "&eMedium &7- expanded mechanics\n" +
                        "&cHard &7- complete boss power sets",
                "&8[EM] &8You have entered an instanced Goblin Kingdom.",
                "&8[EM] &8You have left the Goblin Kingdom.",
                List.of("filename=GK_boss_snickersnap.yml",
                        "filename=GK_boss_grubgulp.yml",
                        "filename=GK_boss_boomfizzle.yml"),
                "goblin_kingdom",
                1,
                false);
        setHasCustomModels(true);
        setDifficulties(List.of(
                Map.of("name", "Easy", "id", "easy"),
                Map.of("name", "Medium", "id", "medium"),
                Map.of("name", "Hard", "id", "hard")));
        setSetupMenuDescription(List.of(
                "&2Instanced realm for 1-5 players.",
                "&7Boss powers scale cumulatively by difficulty."));
        setNightbreakSlug("goblin-kingdom");
    }
}
