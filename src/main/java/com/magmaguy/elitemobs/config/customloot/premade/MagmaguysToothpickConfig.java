package com.magmaguy.elitemobs.config.customloot.premade;

import com.magmaguy.elitemobs.config.customloot.CustomLootConfigFields;
import org.bukkit.Material;

import java.util.Arrays;

public class MagmaguysToothpickConfig extends CustomLootConfigFields {
    public MagmaguysToothpickConfig() {
        super("magmaguys_toothpick",
                true,
                Material.WOODEN_SWORD.toString(),
                "&4Magmaguy&c's &2Toothpick",
                Arrays.asList("&aIn nearly mint condition!"),
                Arrays.asList("VANISHING_CURSE,1"),
                null,
                "dynamic",
                "scalable",
                "custom");
    }
}
