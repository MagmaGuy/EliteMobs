package com.magmaguy.elitemobs.config.dungeonpackager.premade;

import com.magmaguy.elitemobs.config.dungeonpackager.DungeonPackagerConfigFields;
import com.magmaguy.elitemobs.utils.DiscordLinks;
import org.bukkit.World;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Arrays;

public class PrimisAdventure extends DungeonPackagerConfigFields {
    public PrimisAdventure() {
        super("primis_adventure",
                false,
                "&7Primis",
                DungeonPackagerConfigFields.DungeonLocationType.WORLD,
                Arrays.asList("&fA tutorial adventure for new players!",
                        "&6Credits: 69OzCanOfBepis, MagmaGuy"),
                new ArrayList<>(),
                new ArrayList<>(),
                DiscordLinks.premiumMinidungeons,
                DungeonPackagerConfigFields.DungeonSizeCategory.ADVENTURE,
                "em_primis",
                null,
                World.Environment.NORMAL,
                true,
                new Vector(0, 0, 0),
                new Vector(0, 0, 0),
                new Vector(0, 0, 0),
                0D,
                0D,
                0,
                "Difficulty: &2Easy\n" +
                        "$bossCount bosses, from level $lowestTier to $highestTier\n" +
                        "&6A tutorial adventure for new players!",
                "&8[EM] &6Primis awaits, strike the earth!",
                "&8[EM] &6You have left Primis!");
    }
}
