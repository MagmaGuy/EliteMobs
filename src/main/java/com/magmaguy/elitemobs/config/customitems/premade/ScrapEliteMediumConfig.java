package com.magmaguy.elitemobs.config.customitems.premade;

import com.magmaguy.elitemobs.config.customitems.CustomItemsConfigFields;
import com.magmaguy.elitemobs.items.customitems.CustomItem;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ScrapEliteMediumConfig extends CustomItemsConfigFields {
    public ScrapEliteMediumConfig() {
        super("elite_scrap_medium",
                true,
                Material.ORANGE_DYE,
                "&9Medium Elite Scrap",
                Arrays.asList("&fUsed to repair Elite items!", "&fRepairs a large amount!"));
        setItemType(CustomItem.ItemType.UNIQUE);
        setEnchantments(new ArrayList<>(List.of("repair,3")));
        setScalability(CustomItem.Scalability.FIXED);
        setSoulbound(false);
    }
}
