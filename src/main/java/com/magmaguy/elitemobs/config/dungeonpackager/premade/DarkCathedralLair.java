package com.magmaguy.elitemobs.config.dungeonpackager.premade;

import com.magmaguy.elitemobs.config.dungeonpackager.DungeonPackagerConfigFields;
import com.magmaguy.elitemobs.utils.DiscordLinks;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.List;

public class DarkCathedralLair extends DungeonPackagerConfigFields {
    public DarkCathedralLair() {
        super("dark_cathedral_lair",
                true,
                "&2[lvl 040] &8The Dark Cathedral",
                new ArrayList<>(List.of("&fThe first ever EliteMobs Lair!",
                        "&fA classic that all servers need!",
                        "&6Credits: MagmaGuy, 69OzCanOfBepis, Frost, Dali")),
                DiscordLinks.freeMinidungeons,
                DungeonSizeCategory.LAIR,
                "em_the_dark_cathedral",
                World.Environment.NETHER,
                true,
                "em_the_dark_cathedral,47.5,85.2,199.5,163,0",
                0,
                "Difficulty: &cHard\n" +
                        "$bossCount level $highestTier Big Boss!\n" +
                        "&cThe original minidungeon, a challenge for\n" +
                        "&ca group of players or for veterans!",
                "&8[EM] &1An invasion is in progress. &9Stop the insurrection!",
                "&8[EM] &1You managed to hold them back. &9For now...",
                "the_dark_cathedral",
                false);
        setSong("name=elitemobs:dark_cathedral.ambient length=93830");
    }
}
