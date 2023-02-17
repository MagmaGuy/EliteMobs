package com.magmaguy.elitemobs.config.customitems.premade;

import com.magmaguy.elitemobs.config.customitems.CustomItemsConfigFields;
import com.magmaguy.elitemobs.items.customitems.CustomItem;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ScrapEliteLargeConfig extends CustomItemsConfigFields {
    public ScrapEliteLargeConfig() {
        super("elite_scrap_large",
                true,
                Material.RED_DYE,
                "&9Large Elite Scrap",
                Arrays.asList("&fUsed to repair Elite items!", "&fRepairs a huge amount!"));
        setItemType(CustomItem.ItemType.UNIQUE);
        setEnchantments(new ArrayList<>(List.of("repair,4")));
        setScalability(CustomItem.Scalability.FIXED);
        setSoulbound(false);
    }
}
