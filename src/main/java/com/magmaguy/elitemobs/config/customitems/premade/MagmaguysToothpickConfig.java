package com.magmaguy.elitemobs.config.customitems.premade;

import com.magmaguy.elitemobs.config.customitems.CustomItemsConfigFields;
import org.bukkit.Material;

import java.util.List;

public class MagmaguysToothpickConfig extends CustomItemsConfigFields {
    public MagmaguysToothpickConfig() {
        super("magmaguys_toothpick",
                true,
                Material.WOODEN_SWORD,
                "<g:#FF4500:#FF6347>Magmaguy</g>&c's <g:#228B22:#32CD32>Toothpick</g>",
                List.of("&aIn nearly mint condition!"));
        setEnchantments(List.of("VANISHING_CURSE,1"));
        setCustomModelID("elitemobs:equipment/magmaguys_toothpick");
    }
}
