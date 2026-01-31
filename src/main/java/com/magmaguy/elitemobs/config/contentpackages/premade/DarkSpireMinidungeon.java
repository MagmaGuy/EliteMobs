package com.magmaguy.elitemobs.config.contentpackages.premade;

import com.magmaguy.elitemobs.config.contentpackages.ContentPackagesConfigFields;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DarkSpireMinidungeon extends ContentPackagesConfigFields {
    public DarkSpireMinidungeon() {
        super("the_dark_spire",
                true,
                "&2[Dynamic] &6The Dark Spire",
                new ArrayList<>(List.of("&fDescend into the depths of the Dark Spire",
                        "&7Choose your challenge level before entering!",
                        "&6Credits: 69OzCanOfBepis")),
                "https://nightbreak.io/plugin/elitemobs/#the-dark-spire",
                DungeonSizeCategory.MINIDUNGEON,
                "em_dark_spire",
                World.Environment.NETHER,
                true,
                "em_dark_spire,60,97.5,97,110,0",
                "em_dark_spire,47.5,94.5,94.5,110,0",
                1,
                "Difficulty: &4Dynamic content!",
                "&8[EM] &1An invasion is in progress. &9Stop the insurrection!",
                "&8[EM] &1You managed to hold them back. &9For now...",
                List.of("filename=darkspire_level_130_miniboss_phase_brute.yml"),
                "the_dark_spire",
                -1,
                false);

        this.contentType = ContentType.DYNAMIC_DUNGEON;
        setDifficulties(List.of(
                Map.of("name", "normal", "levelSync", "+5", "id", 0),
                Map.of("name", "hard", "levelSync", "+0", "id", 1),
                Map.of("name", "mythic", "levelSync", "-5", "id", 2)));
        setSetupMenuDescription(List.of(
                "&2A Dynamic Minidungeon where you choose the level!",
                "&2Select from levels based on your guild rank!"));
        setDungeonLockoutMinutes(1440);
        setNightbreakSlug("the-dark-spire");
    }
}