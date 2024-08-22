package com.magmaguy.elitemobs.config.dungeonpackager.premade;

import com.magmaguy.elitemobs.config.dungeonpackager.DungeonPackagerConfigFields;
import com.magmaguy.elitemobs.utils.DiscordLinks;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.List;

public class PrimisAdventure extends DungeonPackagerConfigFields {
    public PrimisAdventure() {
        super("primis_adventure",
                true,
                "&2[lvl 000-020] &7Primis",
                new ArrayList<>(List.of("&fA tutorial adventure for new players!",
                        "&6Credits: 69OzCanOfBepis, MagmaGuy")),
                DiscordLinks.premiumMinidungeons,
                DungeonPackagerConfigFields.DungeonSizeCategory.ADVENTURE,
                "em_primis",
                World.Environment.NORMAL,
                true,
                "em_primis,1406.5,21.5,357.5,33,0",
                0,
                "Difficulty: &2Easy\n" +
                        "$bossCount bosses, from level $lowestTier to $highestTier\n" +
                        "&6A tutorial adventure for new players!",
                "&8[EM] &6Primis awaits, strike the earth!",
                "&8[EM] &6You have left Primis!",
                "primis",
                false);
        setWormholeWorldName("em_primis_wormhole");
        setHasCustomModels(true);
    }
}
