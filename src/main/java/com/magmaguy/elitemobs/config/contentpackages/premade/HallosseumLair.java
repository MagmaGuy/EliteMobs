package com.magmaguy.elitemobs.config.contentpackages.premade;

import com.magmaguy.elitemobs.config.contentpackages.ContentPackagesConfigFields;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HallosseumLair extends ContentPackagesConfigFields {
    public HallosseumLair() {
        super("hallosseum_lair",
                true,
                "&2[Dynamic] &cThe Hallosseum",
                new ArrayList<>(List.of("&fThe 2020 spooky halloween encounter!",
                        "&6Credits: MagmaGuy & 69OzCanOfBepis")),
                "https://nightbreak.io/plugin/elitemobs/#hallosseum",
                DungeonSizeCategory.LAIR,
                "em_hallosseum",
                World.Environment.NETHER,
                true,
                "em_hallosseum,66.5,22.2,77.5,20,0",
                "em_hallosseum,62.5,21.2,85.5,18,0",
                0,
                "Difficulty: &chard\n" +
                        "$bossCount level $lowestTier Big Boss!\n" +
                        "&6A fun Halloween challenge!",
                "&8[EM] &4Trick or treat! &8Your soul is mine!",
                "&8[EM] &4You've escaped with your soul intact.",
                List.of("filename=halloween_event_boss_p1.yml"),
                "the_hallosseum",
                -1,
                false);
        this.contentType = ContentType.DYNAMIC_DUNGEON;
        setDifficulties(List.of(
                Map.of("name", "normal", "levelSync", "+5", "id", 0),
                Map.of("name", "hard", "levelSync", "+0", "id", 1),
                Map.of("name", "mythic", "levelSync", "-5", "id", 2)));
        setDungeonLockoutMinutes(1440);
        setSetupMenuDescription(List.of(
                "&2A dynamic Lair!"));
        setNightbreakSlug("hallosseum");
    }
}
