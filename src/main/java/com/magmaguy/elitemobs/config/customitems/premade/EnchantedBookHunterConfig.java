package com.magmaguy.elitemobs.config.customitems.premade;

import com.magmaguy.elitemobs.config.customitems.CustomItemsConfigFields;
import com.magmaguy.elitemobs.items.customitems.CustomItem;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;

public class EnchantedBookHunterConfig extends CustomItemsConfigFields {
    public EnchantedBookHunterConfig() {
        super("enchanted_book_hunter",
                true,
                Material.ENCHANTED_BOOK,
                "&5Elite Hunter Enchanted Book",
                new ArrayList<>(List.of("&2Used to enchant items at the enchanter!")));
        setEnchantments(new ArrayList<>(List.of("elitemobs:hunter,1")));
        setItemType(CustomItem.ItemType.UNIQUE);
        setScalability(CustomItem.Scalability.FIXED);
        setSoulbound(false);
    }
}
