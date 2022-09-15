package com.magmaguy.elitemobs.config.customitems.premade;

import com.magmaguy.elitemobs.config.customitems.CustomItemsConfigFields;
import com.magmaguy.elitemobs.items.customitems.CustomItem;
import org.bukkit.Material;

import java.util.List;

public class GrapplingArrowConfig extends CustomItemsConfigFields {
    public GrapplingArrowConfig() {
        super("grappling_arrow",
                true,
                Material.SPECTRAL_ARROW,
                "&6Grappling Arrow",
                List.of("&eFire at a TARGET BLOCK to grapple!"));
        setEnchantments(List.of("GRAPPLING_HOOK,1"));
        setScalability(CustomItem.Scalability.FIXED);
        setDropWeight("1");
    }
}
