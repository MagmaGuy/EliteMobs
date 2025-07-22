package com.magmaguy.elitemobs.config.contentpackages.premade;

import com.magmaguy.elitemobs.config.contentpackages.ContentPackagesConfigFields;
import org.bukkit.World;

import java.util.List;

public class YggdrasilRealm extends ContentPackagesConfigFields {
    public YggdrasilRealm() {
        super("yggdrasil_realm",
                true,
                "&2[lvl 075-085] &5Yggdrasil",
                List.of("&5A mythic Norse dungeon at the roots of Yggdrasil!",
                        "&5Credits: MagmaGuy, Dali, Frost"),
                "https://nightbreak.io/plugin/elitemobs/#the-vampire-manor",
                DungeonSizeCategory.REALM,
                "em_yggdrasil",
                World.Environment.NORMAL,
                true,
                "em_yggdrasil,-321.5,-22.0,102.5,141.5,0.0",
                0,
                "Difficulty: &6Hard\n" +
                        "$bossCount bosses, from level $lowestTier to $highestTier\n" +
                        "&5A mythic journey through Yggdrasil''s roots\n"+
                        "&5for brave warriors seeking glory!",
                "&8[EM] &5Yggdrasil awaits!",
                "&8[EM] &5You have left Yggdrasil",
                "em_yggdrasil",
                false);
        setSetupMenuDescription(List.of(
                "&2A Realm for players around level 75!"
        ));
    }
}