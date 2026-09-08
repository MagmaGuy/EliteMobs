package com.magmaguy.elitemobs.config.customitems.premade;

import com.magmaguy.elitemobs.config.customitems.CustomItemsConfigFields;
import com.magmaguy.elitemobs.items.customitems.CustomItem;
import org.bukkit.Material;
import java.util.ArrayList;
import java.util.List;

public final class EnchantedBookMulticastConfig extends CustomItemsConfigFields {
    public EnchantedBookMulticastConfig() {
        super("enchanted_book_multicast", true, Material.BOOK, "&5Elite Multicast Enchanted Book",
                new ArrayList<>(List.of("&2Used at the enchanter.", "&7Wands only. Adds extra magic bolts.")));
        setEnchantments(new ArrayList<>(List.of("MULTICAST,1", "ENCHANTED_SOURCE,1")));
        setItemType(CustomItem.ItemType.UNIQUE);
        setScalability(CustomItem.Scalability.FIXED);
        setSoulbound(false);
    }
}
