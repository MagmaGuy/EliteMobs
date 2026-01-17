package com.magmaguy.elitemobs.config.contentpackages.premade;

import com.magmaguy.elitemobs.config.contentpackages.ContentPackagesConfigFields;
import org.bukkit.World;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class YggdrasilRealm extends ContentPackagesConfigFields {
    public YggdrasilRealm() {
        super("yggdrasil_realm",
                true,
                "&5Yggdrasil",
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
        setContentType(ContentType.DYNAMIC_DUNGEON);
        setContentLevel(-1);
        setDifficulties(List.of(
                Map.of("name", "normal", "levelSync", "+5", "id", 0),
                Map.of("name", "hard", "levelSync", "+0", "id", 1),
                Map.of("name", "mythic", "levelSync", "-5", "id", 2)));
        setDungeonLockoutMinutes(1440);
        setSetupMenuDescription(List.of(
                "&2A dynamic Realm!"
        ));
    }
}