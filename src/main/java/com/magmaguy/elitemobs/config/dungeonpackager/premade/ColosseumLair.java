package com.magmaguy.elitemobs.config.dungeonpackager.premade;

import com.magmaguy.elitemobs.config.dungeonpackager.DungeonPackagerConfigFields;

import java.util.Arrays;

public class ColosseumLair extends DungeonPackagerConfigFields {
    public ColosseumLair() {
        super("colosseum_lair",
                true,
                "&6The Colosseum",
                DungeonLocationType.SCHEMATIC,
                Arrays.asList("&fFeaturing the first true World boss, first",
                        "&fmulti-phased battle, first mounted boss,",
                        "&ffirst disguised boss... a truly epic fight!",
                        "&6Credits: MagmaGuy & Maldini"),
                Arrays.asList("colosseum_pit_master.yml:0,0,0,0,0"),
                Arrays.asList(),
                "patreon.com/magmaguy",
                DungeonSizeCategory.LAIR,
                null,
                "elitemobs_colosseum.schem",
                null,
                null);
    }
}
