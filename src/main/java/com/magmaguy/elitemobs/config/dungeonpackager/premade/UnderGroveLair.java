package com.magmaguy.elitemobs.config.dungeonpackager.premade;

import com.magmaguy.elitemobs.config.dungeonpackager.DungeonPackagerConfigFields;
import com.magmaguy.elitemobs.utils.DiscordLinks;
import org.bukkit.World;

import java.util.Arrays;

public class UnderGroveLair extends DungeonPackagerConfigFields {
    public UnderGroveLair() {
        super("under_grove_lair",
                false,
                "&6The Under Grove",
                Arrays.asList("&6Stop the dryad's ritual!"),
                DiscordLinks.premiumMinidungeons,
                DungeonSizeCategory.LAIR,
                "em_under_grove",
                World.Environment.NORMAL,
                true,
                "em_under_grove,-2.5,-40.5,35,90,0",
                0,
                "Difficulty: &cHard\n" +
                        "$bossCount level $lowestTier Big Boss!\n" +
                        "&6The dryads are up to no good, stop them!",
                "&8[EM] &aYou are now trespassing the sacred grove!",
                "&8[EM] &aYou have left the sacred grove!",
                "the_under_grove");
    }
}
