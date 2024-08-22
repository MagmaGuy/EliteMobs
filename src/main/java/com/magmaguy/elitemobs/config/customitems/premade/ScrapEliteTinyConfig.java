package com.magmaguy.elitemobs.config.customitems.premade;

import com.magmaguy.elitemobs.config.customitems.CustomItemsConfigFields;
import com.magmaguy.elitemobs.items.customitems.CustomItem;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;

public class ScrapEliteTinyConfig extends CustomItemsConfigFields {
    public ScrapEliteTinyConfig() {
        super("elite_scrap_tiny",
                true,
                Material.WHITE_DYE,
                "&fTiny Elite Scrap",
                new ArrayList<>(List.of("&fUsed to repair Elite items!", "&fRepairs a small amount!")));
        setItemType(CustomItem.ItemType.UNIQUE);
        setEnchantments(new ArrayList<>(List.of("repair,1")));
        setScalability(CustomItem.Scalability.FIXED);
        setSoulbound(false);
    }
}
