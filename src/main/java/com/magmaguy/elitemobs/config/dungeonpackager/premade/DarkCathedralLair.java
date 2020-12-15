package com.magmaguy.elitemobs.config.dungeonpackager.premade;

import com.magmaguy.elitemobs.config.dungeonpackager.DungeonPackagerConfigFields;
import org.bukkit.util.Vector;

import java.util.Arrays;

public class DarkCathedralLair extends DungeonPackagerConfigFields {
    public DarkCathedralLair() {
        super("dark_cathedral_lair",
                false,
                "&8The Dark Cathedral",
                DungeonLocationType.SCHEMATIC,
                Arrays.asList("&fThe first ever EliteMobs Lair!",
                        "&fA classic that all servers need!",
                        "&6Credits: MagmaGuy & 69OzCanOfBepis"),
                Arrays.asList("dark_cathedral_tier_75_boss.yml:0.5,0.5,24.5"),
                Arrays.asList(""),
                "https://discord.gg/vRW9wXhK",
                DungeonSizeCategory.LAIR,
                null,
                "elitemobs_dark_cathedral.schem",
                null,
                true,
                new Vector(17, -27, 0),
                new Vector(-19, 54, 32),
                new Vector(0, 0, 0),
                null,
                null,
                3,
                "Difficulty: &cHard\n" +
                        "$bossCount level $highestTier Big Boss!\n" +
                        "&cThe original minidungeon, a challenge for\n" +
                        "&ca small group of players or for veterans!");
    }
}
