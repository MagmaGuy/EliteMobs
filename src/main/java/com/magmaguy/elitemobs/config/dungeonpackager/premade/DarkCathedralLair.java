package com.magmaguy.elitemobs.config.dungeonpackager.premade;

import com.magmaguy.elitemobs.config.dungeonpackager.DungeonPackagerConfigFields;

import java.util.Arrays;

public class DarkCathedralLair extends DungeonPackagerConfigFields {
    public DarkCathedralLair() {
        super("dark_cathedral_lair",
                true,
                "&8The Dark Cathedral",
                DungeonLocationType.SCHEMATIC,
                Arrays.asList("&fThe first ever EliteMobs Lair!",
                        "&fA classic that all servers need!",
                        "&6Credits: MagmaGuy & 69OzCanOfBepis"),
                Arrays.asList("priest_of_cthulhu.yml:0,0,0,0,0"),
                Arrays.asList(""),
                "https://discord.gg/vRW9wXhK",
                DungeonSizeCategory.LAIR,
                null,
                "elitemobs_dark_cathedral.schem",
                null,
                null);
    }
}
