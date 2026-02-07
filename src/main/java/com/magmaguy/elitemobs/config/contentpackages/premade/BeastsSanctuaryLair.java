package com.magmaguy.elitemobs.config.contentpackages.premade;

import com.magmaguy.elitemobs.config.contentpackages.ContentPackagesConfigFields;
import org.bukkit.World;

import java.util.List;
import java.util.Map;

public class BeastsSanctuaryLair extends ContentPackagesConfigFields {
    public BeastsSanctuaryLair() {
        super("beasts_sanctuary_lair",
                true,
                "&2[Dynamic] &cThe Beast's Sanctuary",
                List.of("&fA dark sanctuary corrupted by",
                        "&fthe terrifying Beast."),
                "https://nightbreak.io/plugin/elitemobs/#the-beasts-sanctuary",
                DungeonSizeCategory.LAIR,
                "em_beasts_sanctuary",
                World.Environment.NORMAL,
                true,
                "em_beasts_sanctuary,35.5,88.2,20.5,46,23",
                "em_beasts_sanctuary,32.5,88.2,36.5,0,0",
                1,
                "Difficulty: &4Dynamic content!",
                "&8[EM] &aYou have entered the Beast Sanctuary! Beware of what prowls here!",
                "&8[EM] &aYou've left the Beast Sanctuary! Did you take trophies?",
                List.of("filename=the_beasts_sanctuary_beast_human_p1.yml"),
                "the_beasts_sanctuary",
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
        setNightbreakSlug("the-beasts-sanctuary");
    }
}
