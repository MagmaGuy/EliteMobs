package com.magmaguy.elitemobs.config.contentpackages.premade;

import com.magmaguy.elitemobs.config.contentpackages.ContentPackagesConfigFields;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class InvasionMinidungeon extends ContentPackagesConfigFields {
    public InvasionMinidungeon() {
        super("invasion_minidungeon",
                true,
                "&2[Dynamic] &6The Invasion",
                new ArrayList<>(List.of("&fThe Halloween Minidungeon!",
                        "&6Credits: MagmaGuy & 69OzCanOfBepis")),
                "https://nightbreak.io/plugin/elitemobs/#the-invasion",
                DungeonSizeCategory.MINIDUNGEON,
                "em_invasion",
                World.Environment.NORMAL,
                true,
                "em_invasion,-8.5,13.2,19.5,80,0",
                "em_invasion,-13.5,12.2,20.5,165,0",
                0,
                "Difficulty: &6Medium\n" +
                        "$bossCount aliens, from level $lowestTier to $highestTier\n" +
                        "&6Don't get abducted!",
                "&8[EM] &7Alien invasion in progress! Defeat the Mothership!",
                "&8[EM] &7You have escaped the alien abductions! No one will ever believe you.",
                List.of("filename=invasion_green_man_spaceship_phase_1.yml"),
                "invasion",
                -1,
                false);
        this.contentType = ContentType.DYNAMIC_DUNGEON;
        setDifficulties(List.of(
                Map.of("name", "normal", "levelSync", "+5", "id", 0),
                Map.of("name", "hard", "levelSync", "+0", "id", 1),
                Map.of("name", "mythic", "levelSync", "-5", "id", 2)));
        setDungeonLockoutMinutes(1440);
        setSetupMenuDescription(List.of(
                "&2A dynamic Minidungeon!"));
        setNightbreakSlug("the-invasion");
    }
}
