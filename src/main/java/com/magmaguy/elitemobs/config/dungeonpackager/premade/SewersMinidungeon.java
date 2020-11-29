package com.magmaguy.elitemobs.config.dungeonpackager.premade;

import com.magmaguy.elitemobs.config.dungeonpackager.DungeonPackagerConfigFields;
import org.bukkit.World;

import java.util.Arrays;

public class SewersMinidungeon extends DungeonPackagerConfigFields {
    public SewersMinidungeon() {
        super("sewers_minidungeon",
                true,
                "&8The Sewers",
                DungeonLocationType.WORLD,
                Arrays.asList("&fThe biggest minidungeon ever made!",
                        "&6Credits: MagmaGuy & 69OzCanOfBepis"),
                Arrays.asList(""),
                Arrays.asList(""),
                "https://discord.gg/vRW9wXhK",
                DungeonSizeCategory.MINIDUNGEON,
                "elitemobs_sewer_maze",
                null,
                World.Environment.NORMAL,
                true);
    }
}
