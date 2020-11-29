package com.magmaguy.elitemobs.config.dungeonpackager.premade;

import com.magmaguy.elitemobs.config.dungeonpackager.DungeonPackagerConfigFields;
import org.bukkit.World;

import java.util.Arrays;

public class HallosseumLair extends DungeonPackagerConfigFields {
    public HallosseumLair() {
        super("hallosseum_lair",
                true,
                "&fHallosseum",
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
                true);
    }
}
