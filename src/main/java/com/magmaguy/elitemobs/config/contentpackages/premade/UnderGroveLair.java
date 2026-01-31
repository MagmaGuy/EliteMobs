package com.magmaguy.elitemobs.config.contentpackages.premade;

import com.magmaguy.elitemobs.config.contentpackages.ContentPackagesConfigFields;
import org.bukkit.World;

import java.util.List;
import java.util.Map;

public class UnderGroveLair extends ContentPackagesConfigFields {
    public UnderGroveLair() {
        super("under_grove_lair",
                true,
                "&2[Dynamic] &6The Under Grove",
                List.of("&6Stop the dryad's ritual!"),
                "https://nightbreak.io/plugin/elitemobs/#the-under-grove",
                DungeonSizeCategory.LAIR,
                "em_under_grove",
                World.Environment.NORMAL,
                true,
                "em_under_grove,-2.5,-38.8,35.5,90,0",
                "em_under_grove,-2.5,-38.8,35.5,90,0",
                1,
                "Difficulty: &4Dynamic content!",
                "&8[EM] &aYou are now trespassing the sacred grove!",
                "&8[EM] &aYou have left the sacred grove!",
                List.of("filename=the_under_grove_rotroot_dryad_p1.yml"),
                "the_under_grove",
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
        setNightbreakSlug("the-under-grove");
    }
}
