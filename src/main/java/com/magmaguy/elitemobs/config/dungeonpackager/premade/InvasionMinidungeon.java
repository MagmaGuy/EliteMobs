package com.magmaguy.elitemobs.config.dungeonpackager.premade;

import com.magmaguy.elitemobs.config.dungeonpackager.DungeonPackagerConfigFields;
import com.magmaguy.elitemobs.utils.DiscordLinks;
import org.bukkit.World;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Arrays;

public class InvasionMinidungeon extends DungeonPackagerConfigFields {
    public InvasionMinidungeon(){
        super("invasion_minidungeon",
                false,
                "The Invasion",
                DungeonLocationType.WORLD,
                Arrays.asList("&fThe Halloween Minidungeon!",
                        "&6Credits: MagmaGuy & 69OzCanOfBepis"),
                new ArrayList<>(),
                new ArrayList<>(),
                DiscordLinks.freeMinidungeons,
                DungeonSizeCategory.MINIDUNGEON,
                "em_invasion",
                null,
                World.Environment.NORMAL,
                true,
                null,
                null,
                new Vector(0, 0, 0),
                0D,
                0D,
                1,
                "Difficulty: &6Medium\n" +
                        "$bossCount aliens, from level $lowestTier to $highestTier\n" +
                        "&6Don't get abducted!",
                "&8[EM] &7Alien invasion in progress! Defeat the Mothership!",
                "&8[EM] &7You have escaped the alien abductions! No one will ever believe you.");
    }
}
