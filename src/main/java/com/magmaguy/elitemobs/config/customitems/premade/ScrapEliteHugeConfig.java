package com.magmaguy.elitemobs.config.customitems.premade;

import com.magmaguy.elitemobs.config.customitems.CustomItemsConfigFields;
import com.magmaguy.elitemobs.items.customitems.CustomItem;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ScrapEliteHugeConfig extends CustomItemsConfigFields {
    public ScrapEliteHugeConfig() {
        super("elite_scrap_huge",
                true,
                Material.PURPLE_DYE,
                "&5Huge Elite Scrap",
                Arrays.asList("&fUsed to repair Elite items!", "&fFully repairs an item!"));
        setItemType(CustomItem.ItemType.UNIQUE);
        setEnchantments(new ArrayList<>(List.of("repair,5")));
        setScalability(CustomItem.Scalability.FIXED);
        setSoulbound(false);
    }
}
