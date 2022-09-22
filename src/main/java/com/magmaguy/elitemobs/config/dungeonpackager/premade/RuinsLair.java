package com.magmaguy.elitemobs.config.dungeonpackager.premade;

import com.magmaguy.elitemobs.config.dungeonpackager.DungeonPackagerConfigFields;
import com.magmaguy.elitemobs.dungeons.SchematicPackage;
import com.magmaguy.elitemobs.utils.DiscordLinks;
import org.bukkit.util.Vector;

import java.util.Arrays;
import java.util.List;

public class RuinsLair extends DungeonPackagerConfigFields {
    public RuinsLair() {
        super("the_ruins",
                false,
                "&6The Ruins",
                Arrays.asList("&fA fight against a myth from",
                        "&fNorse mythology, be prepared for a smiting!",
                        "&6Credits: 69OzCanOfBepis, MagmaGuy"),
                List.of(
                        "ruins_boss_p1.yml:0,0,0"),
                List.of(),
                DiscordLinks.premiumMinidungeons,
                DungeonSizeCategory.LAIR,
                "em_the_ruins.schem",
                true,
                new Vector(18, -6, -19),
                new Vector(-21, 10, 20),
                "-1,-4,18,0,0",
                0,
                "Difficulty: &cHard\n" +
                        "$bossCount level $lowestTier Big Boss!\n" +
                        "&cA tough fight against a Norse god!",
                "&8[EM] &3Those who challenge the myths must be prepared for their downfall!",
                "&8[EM] &3You now know what it takes to make a legend!",
                SchematicPackage.SchematicRotation.SOUTH.toString(),
                "the_ruins");
    }
}
