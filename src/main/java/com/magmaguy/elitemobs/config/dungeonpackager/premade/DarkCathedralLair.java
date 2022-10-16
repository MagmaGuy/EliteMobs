package com.magmaguy.elitemobs.config.dungeonpackager.premade;

import com.magmaguy.elitemobs.config.dungeonpackager.DungeonPackagerConfigFields;
import com.magmaguy.elitemobs.dungeons.SchematicPackage;
import com.magmaguy.elitemobs.utils.DiscordLinks;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DarkCathedralLair extends DungeonPackagerConfigFields {
    public DarkCathedralLair() {
        super("dark_cathedral_lair",
                false,
                "&8The Dark Cathedral",
                Arrays.asList("&fThe first ever EliteMobs Lair!",
                        "&fA classic that all servers need!",
                        "&6Credits: MagmaGuy & 69OzCanOfBepis"),
                List.of("dark_cathedral_tier_75_boss.yml:0.5,0.5,24.5"),
                new ArrayList<>(),
                DiscordLinks.freeMinidungeons,
                DungeonSizeCategory.LAIR,
                "em_dark_cathedral.schem",
                true,
                new Vector(17, -27, 0),
                new Vector(-19, 54, 32),
                "0,0,0,0,0",
                0,
                "Difficulty: &cHard\n" +
                        "$bossCount level $highestTier Big Boss!\n" +
                        "&cThe original minidungeon, a challenge for\n" +
                        "&ca group of players or for veterans!",
                "&8[EM] &8Welcome to the Dark Cathedral. &4Death awaits inside...",
                "&8[EM] &8Now leaving the Dark Cathedral. &4It awaits your return...",
                SchematicPackage.SchematicRotation.SOUTH.toString(),
                "the_dark_cathedral");
    }
}
