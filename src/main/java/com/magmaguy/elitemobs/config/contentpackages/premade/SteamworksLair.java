package com.magmaguy.elitemobs.config.contentpackages.premade;

import com.magmaguy.elitemobs.config.contentpackages.ContentPackagesConfigFields;
import org.bukkit.World;

import java.util.List;
import java.util.Map;

public class SteamworksLair extends ContentPackagesConfigFields {
    public SteamworksLair() {
        super("steamworks_lair",
                true,
                "&2[Dynamic] &6The Steamworks",
                List.of("&fA steampunk inspired lair!"),
                "https://nightbreak.io/plugin/elitemobs/#the-steamworks",
                DungeonSizeCategory.LAIR,
                "em_steamworks_lair",
                World.Environment.NORMAL,
                true,
                "em_steamworks_lair,-48.5,-47.8,-102.5,40,0",
                "em_steamworks_lair,-48.5,-47.8,-102.5,40,0",
                1,
                "Difficulty: &4Dynamic content!",
                "&6Reach the top and mind your step!",
                "&6Now leaving the clockwork dominion!",
                List.of("filename=the_steamworks_clk_wrx702_p1.yml"),
                "the_steamworks",
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
        setNightbreakSlug("the-steamworks");
    }
}
