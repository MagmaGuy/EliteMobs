package com.magmaguy.elitemobs.config.contentpackages.premade;

import com.magmaguy.elitemobs.config.contentpackages.ContentPackagesConfigFields;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.List;

public class OasisAdventure extends ContentPackagesConfigFields {
    public OasisAdventure() {
        super("oasis_adventure",
                true,
                "&2[lvl 020-055] &6The Oasis",
                new ArrayList<>(List.of("&fA massive adventure for beginners!",
                        "&6Credits: 69OzCanOfBepis")),
                "https://nightbreak.io/plugin/elitemobs/#the-oasis-adventure",
                DungeonSizeCategory.ADVENTURE,
                "em_oasis",
                World.Environment.NORMAL,
                true,
                "em_oasis,260.5,22,280.5,0,0",
                2,
                "Difficulty: &2Solo-able\n" +
                        "$bossCount bosses, from level $lowestTier to $highestTier\n" +
                        "&6A big adventure for people who know the ropes!",
                "&8[EM] &6You have arrived at the Oasis!",
                "&8[EM] &6You have left the Oasis!",
                "oasis",
                false);
        setWormholeWorldName("em_oasis_wormhole");

    }
}
