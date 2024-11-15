package com.magmaguy.elitemobs.config.contentpackages.premade;

import com.magmaguy.elitemobs.config.contentpackages.ContentPackagesConfigFields;
import org.bukkit.World;

import java.util.List;

public class VampireManorDungeon extends ContentPackagesConfigFields {
    public VampireManorDungeon() {
        super("vampire_manor_lair",
                true,
                "&2[lvl 120] &4The Vampire Manor",
                List.of("&6Defeat the Vampire King!"),
                "https://nightbreak.io/plugin/elitemobs/#the-vampire-manor",
                DungeonSizeCategory.LAIR,
                "em_the_vampire_manor",
                World.Environment.NETHER,
                true,
                "em_the_vampire_manor,281.5,31.0,1587.5,-180,0",
                0,
                "Difficulty: &cHard\n" +
                        "$bossCount level $lowestTier Big Boss!\n" +
                        "&6The Vampire King has arisen, stop his summoning!",
                "&8[EM] &aYou are now trespassing the sacred grove!",
                "&8[EM] &aYou have left the sacred grove!",
                "vampire_manor",
                false);
        setSetupMenuDescription(List.of(
                "&2A Lair for players around level 120-140!"
        ));
    }
}
