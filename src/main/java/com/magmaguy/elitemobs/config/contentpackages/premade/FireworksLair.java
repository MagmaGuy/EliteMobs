package com.magmaguy.elitemobs.config.contentpackages.premade;

import com.magmaguy.elitemobs.config.contentpackages.ContentPackagesConfigFields;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FireworksLair extends ContentPackagesConfigFields {
    public FireworksLair() {
        super("fireworks_lair",
                true,
                "&2[Dynamic] &aThe Fireworks",
                new ArrayList<>(List.of("&fThe 2021 4th of July map!",
                        "&6Credits: MagmaGuy")),
                "https://nightbreak.io/plugin/elitemobs/#fireworks",
                DungeonSizeCategory.LAIR,
                "em_fireworks",
                World.Environment.NORMAL,
                true,
                "em_fireworks,30.5,65.2,-32.5,45,0",
                "em_fireworks,24.5,65.2,-28.5,45,0",
                0,
                "Difficulty: &cHard\n" +
                        "$bossCount level $lowestTier Big Boss!\n" +
                        "&6An encounter full of explosions!",
                "&8[EM] &eWelcome to the fireworks show!",
                "&8[EM] &eYou've left the party!",
                List.of("filename=fireworks_level_50_boss_phase_1.yml"),
                "the_fireworks",
                -1,
                true);
        this.contentType = ContentType.DYNAMIC_DUNGEON;
        setDifficulties(List.of(
                Map.of("name", "normal", "levelSync", "+5", "id", 0),
                Map.of("name", "hard", "levelSync", "+0", "id", 1),
                Map.of("name", "mythic", "levelSync", "-5", "id", 2)));
        setDungeonLockoutMinutes(1440);
        setSetupMenuDescription(List.of(
                "&2A dynamic Lair!"));
        setNightbreakSlug("fireworks");
    }
}
