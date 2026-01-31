package com.magmaguy.elitemobs.config.contentpackages.premade;

import com.magmaguy.elitemobs.config.contentpackages.ContentPackagesConfigFields;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DarkCathedralLair extends ContentPackagesConfigFields {
    public DarkCathedralLair() {
        super("dark_cathedral_lair",
                true,
                "&2[Dynamic] &8The Dark Cathedral",
                new ArrayList<>(List.of("&fThe first ever EliteMobs Lair!",
                        "&fA classic that all servers need!",
                        "&6Credits: MagmaGuy, 69OzCanOfBepis, Frost, Dali")),
                "https://nightbreak.io/plugin/elitemobs/#the-dark-cathedral",
                DungeonSizeCategory.LAIR,
                "em_the_dark_cathedral",
                World.Environment.NETHER,
                true,
                "em_the_dark_cathedral,47.5,85.2,199.5,163,0",
                "em_the_dark_cathedral,43.5,84.2,191.5,168,0",
                0,
                "Difficulty: &cHard\n" +
                        "$bossCount level $highestTier Big Boss!\n" +
                        "&cThe original minidungeon, a challenge for\n" +
                        "&ca group of players or for veterans!",
                "&8[EM] &1An invasion is in progress. &9Stop the insurrection!",
                "&8[EM] &1You managed to hold them back. &9For now...",
                List.of("filename=dark_cathedral_boss.yml"),
                "the_dark_cathedral",
                -1,
                false);
        setSong("name=elitemobs:dark_cathedral.ambient length=93830");
        setSetupMenuDescription(List.of(
                "&2A Lair for players around level 15!",
                "&2The perfect quick showcase for EliteMobs,",
                "&2with custom models, a custom soundtrack",
                "&2and custom powers, all for free!"));
        setDifficulties(List.of(
                Map.of("name", "normal", "levelSync", "+5", "id", 0),
                Map.of("name", "hard", "levelSync", "0", "id", 1),
                Map.of("name", "mythic", "levelSync", "-5", "id", 2)));
        this.contentType = ContentType.DYNAMIC_DUNGEON;
        setDungeonLockoutMinutes(1440);
        setNightbreakSlug("the-dark-cathedral");
    }
}
