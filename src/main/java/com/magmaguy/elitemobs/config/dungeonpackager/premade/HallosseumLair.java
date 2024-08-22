package com.magmaguy.elitemobs.config.dungeonpackager.premade;

import com.magmaguy.elitemobs.config.dungeonpackager.DungeonPackagerConfigFields;
import com.magmaguy.elitemobs.utils.DiscordLinks;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.List;

public class HallosseumLair extends DungeonPackagerConfigFields {
    public HallosseumLair() {
        super("hallosseum_lair",
                true,
                "&2[lvl 030] &cThe Hallosseum",
                new ArrayList<>(List.of("&fThe 2020 spooky halloween encounter!",
                        "&6Credits: MagmaGuy & 69OzCanOfBepis")),
                DiscordLinks.freeMinidungeons,
                DungeonSizeCategory.LAIR,
                "em_hallosseum",
                World.Environment.NETHER,
                true,
                "em_hallosseum,66.5,22.2,77.5,20,0",
                0,
                "Difficulty: &chard\n" +
                        "$bossCount level $lowestTier Big Boss!\n" +
                        "&6A fun Halloween challenge!",
                "&8[EM] &4Trick or treat! &8Your soul is mine!",
                "&8[EM] &4You've escaped with your soul intact.",
                "the_hallosseum",
                false);
    }
}
