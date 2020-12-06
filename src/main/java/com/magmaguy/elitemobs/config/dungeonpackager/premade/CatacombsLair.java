package com.magmaguy.elitemobs.config.dungeonpackager.premade;

import com.magmaguy.elitemobs.config.dungeonpackager.DungeonPackagerConfigFields;
import org.bukkit.util.Vector;

import java.util.Arrays;

public class CatacombsLair extends DungeonPackagerConfigFields {
    public CatacombsLair() {
        super("catacombs_lair",
                true,
                "&fThe Catacombs",
                DungeonLocationType.SCHEMATIC,
                Arrays.asList("&fThe best starter dungeon for players!",
                        "&6Credits: Realm of Lotheridon"),
                Arrays.asList("null.yml:0,0,0"),
                Arrays.asList(),
                "patreon.com/magmaguy",
                DungeonSizeCategory.LAIR,
                null,
                "elitemobs_catacombs.schem",
                null,
                true,
                new Vector(-41, 14, 0),
                new Vector(40, 40, 88));
    }
}
