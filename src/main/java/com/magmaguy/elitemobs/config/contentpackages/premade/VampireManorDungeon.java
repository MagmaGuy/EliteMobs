package com.magmaguy.elitemobs.config.contentpackages.premade;

import com.magmaguy.elitemobs.config.contentpackages.ContentPackagesConfigFields;
import org.bukkit.World;

import java.util.List;
import java.util.Map;

public class VampireManorDungeon extends ContentPackagesConfigFields {
    public VampireManorDungeon() {
        super("vampire_manor_lair",
                true,
                "&2[Dynamic] &4The Vampire Manor",
                List.of("&6Defeat the Vampire King!"),
                "https://nightbreak.io/plugin/elitemobs/#the-vampire-manor",
                DungeonSizeCategory.LAIR,
                "em_the_vampire_manor",
                World.Environment.NETHER,
                true,
                "em_the_vampire_manor,281.5,31.0,1587.5,-180,0",
                "em_the_vampire_manor,281.5,31.0,1587.5,-180,0",
                1,
                "Difficulty: &4Dynamic content!",
                "&8[EM] &4You have entered the Vampire Manor!",
                "&8[EM] &4You have left the Vampire Manor!",
                List.of("filename=vampire_king_final_boss.yml"),
                "vampire_manor",
                -1,
                false);

        this.contentType = ContentType.DYNAMIC_DUNGEON;

        setDifficulties(List.of(
                Map.of("name", "normal", "levelSync", "+5", "id", 0),
                Map.of("name", "hard", "levelSync", "+0", "id", 1),
                Map.of("name", "mythic", "levelSync", "-5", "id", 2)));
        setSetupMenuDescription(List.of(
                "&2A Dynamic Lair where you choose the level!"));
        setDungeonLockoutMinutes(1440);
        setNightbreakSlug("the-vampire-manor");
    }
}
