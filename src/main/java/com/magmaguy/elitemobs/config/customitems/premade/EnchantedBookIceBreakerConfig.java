package com.magmaguy.elitemobs.config.customitems.premade;

import com.magmaguy.elitemobs.config.customitems.CustomItemsConfigFields;
import com.magmaguy.elitemobs.items.customitems.CustomItem;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;

public class EnchantedBookIceBreakerConfig extends CustomItemsConfigFields {
    public EnchantedBookIceBreakerConfig(){
        super("enchanted_book_ice_breaker",
                true,
                Material.BOOK,
                "&5Elite Ice Breaker Enchanted Book",
                new ArrayList<>(List.of("&2Used to enchant items at the enchanter!")));
        setEnchantments(new ArrayList<>(List.of("ICE_BREAKER,1", "ENCHANTED_SOURCE,1")));
        setItemType(CustomItem.ItemType.UNIQUE);
        setScalability(CustomItem.Scalability.FIXED);
        setSoulbound(false);
    }
}
