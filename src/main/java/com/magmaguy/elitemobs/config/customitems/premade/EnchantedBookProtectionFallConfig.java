package com.magmaguy.elitemobs.config.customitems.premade;

import com.magmaguy.elitemobs.config.customitems.CustomItemsConfigFields;
import com.magmaguy.elitemobs.items.customitems.CustomItem;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;

public class EnchantedBookProtectionFallConfig extends CustomItemsConfigFields {
    public EnchantedBookProtectionFallConfig(){
        super("enchanted_book_protection_fall",
                true,
                Material.BOOK,
                "&5Elite Feather Falling Enchanted Book",
                new ArrayList<>(List.of("&2Used to enchant items at the enchanter!")));
        setEnchantments(new ArrayList<>(List.of("PROTECTION_FALL,1", "ENCHANTED_SOURCE,1")));
        setItemType(CustomItem.ItemType.UNIQUE);
        setScalability(CustomItem.Scalability.FIXED);
        setSoulbound(false);
    }
}
