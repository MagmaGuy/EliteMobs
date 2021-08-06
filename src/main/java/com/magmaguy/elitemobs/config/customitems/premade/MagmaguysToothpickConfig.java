package com.magmaguy.elitemobs.config.customitems.premade;

import com.magmaguy.elitemobs.config.customitems.CustomItemsConfigFields;
import org.bukkit.Material;

import java.util.Arrays;

public class MagmaguysToothpickConfig extends CustomItemsConfigFields {
    public MagmaguysToothpickConfig() {
        super("magmaguys_toothpick",
                true,
                Material.WOODEN_SWORD,
                "&4Magmaguy&c's &2Toothpick",
                Arrays.asList("&aIn nearly mint condition!"));
        setEnchantments(Arrays.asList("VANISHING_CURSE,1"));
        setCustomModelID(1);
    }
}
