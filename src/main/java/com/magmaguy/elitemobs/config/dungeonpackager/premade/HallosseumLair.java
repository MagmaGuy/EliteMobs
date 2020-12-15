package com.magmaguy.elitemobs.config.dungeonpackager.premade;

import com.magmaguy.elitemobs.config.dungeonpackager.DungeonPackagerConfigFields;
import org.bukkit.World;
import org.bukkit.util.Vector;

import java.util.Arrays;

public class HallosseumLair extends DungeonPackagerConfigFields {
    public HallosseumLair() {
        super("hallosseum_lair",
                false,
                "&cHallosseum",
                DungeonLocationType.WORLD,
                Arrays.asList("&fThe 2020 spooky halloween encounter!",
                        "&6Credits: MagmaGuy & 69OzCanOfBepis"),
                Arrays.asList(""),
                Arrays.asList(""),
                "https://discord.gg/vRW9wXhK",
                DungeonSizeCategory.LAIR,
                "hallosseum",
                null,
                World.Environment.NETHER,
                true,
                null,
                null,
                new Vector(0, 0, 0),
                -130D,
                0D,
                2,
                "Difficulty: &6Medium\n" +
                        "$bossCount level $lowestTier Big Boss!\n" +
                        "&6A fun Halloween challenge!");
    }
}
