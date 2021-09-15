package com.magmaguy.elitemobs.config.dungeonpackager.premade;

import com.magmaguy.elitemobs.config.dungeonpackager.DungeonPackagerConfigFields;
import com.magmaguy.elitemobs.utils.DiscordLinks;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Arrays;

public class VampireManorMinidungeon extends DungeonPackagerConfigFields {
    public VampireManorMinidungeon(){
        super("vampire_manor",
                false,
                "&cThe Vampire Manor",
                DungeonLocationType.SCHEMATIC,
                Arrays.asList("&fPrepare to spill blood!",
                        "&6Credits: MagmaGuy, 69OzCanOfBepis"),
                new ArrayList<>(),
                Arrays.asList(""),
                DiscordLinks.premiumMinidungeons,
                DungeonSizeCategory.MINIDUNGEON,
                null,
                "em_vampire_manor.schem",
                null,
                true,
                new Vector(67, -28, 0),
                new Vector(-57, 112, 97),
                new Vector(0, 0, 0),
                0D,
                0D,
                0,
                "Difficulty: &6Hard\n" +
                        "$bossCount bosses, from level $lowestTier to $highestTier\n" +
                        "&6A deadly manor full of blood thristy creatures!\n" +
                        "&6Are you ready to face that which haunts the night?",
                "&8[EM] &cTrespassing on dangerous grounds!!",
                "&8[EM] &cNow leaving haunted land!");
    }
}
