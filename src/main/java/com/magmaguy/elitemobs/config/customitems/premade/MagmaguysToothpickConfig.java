package com.magmaguy.elitemobs.config.customitems.premade;

import com.magmaguy.elitemobs.config.customitems.CustomItemsConfigFields;
import org.bukkit.Material;

import java.util.List;

public class MagmaguysToothpickConfig extends CustomItemsConfigFields {
    public MagmaguysToothpickConfig() {
        super("magmaguys_toothpick",
                true,
                Material.WOODEN_SWORD,
                "&4Magmaguy&c's &2Toothpick",
                List.of("&aIn nearly mint condition!"));
        setEnchantments(List.of("VANISHING_CURSE,1"));
        setCustomModelID("elitemobs:equipment/magmaguys_toothpick");
    }
}
