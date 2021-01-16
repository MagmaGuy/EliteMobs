package com.magmaguy.elitemobs.config.customloot.premade;

import com.magmaguy.elitemobs.config.customloot.CustomLootConfigFields;
import org.bukkit.Material;

import java.util.Arrays;

public class SummonWolfScrollConfig extends CustomLootConfigFields {
    public SummonWolfScrollConfig() {
        super("summon_wolf_scroll",
                true,
                Material.PAPER.toString(),
                "&6Summon Wolf Scroll",
                Arrays.asList("&aSummons a good boy", "&ato help you bite Elites!", "&aMake sure you pet it!"),
                Arrays.asList("SUMMON_WOLF,1"),
                null,
                null,
                null,
                "unique");
    }
}
