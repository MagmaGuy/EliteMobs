package com.magmaguy.elitemobs.config.customitems.premade;

import com.magmaguy.elitemobs.config.customitems.CustomItemsConfigFields;
import com.magmaguy.elitemobs.items.customitems.CustomItem;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;

public class EnchantedBookSoulSpeedConfig extends CustomItemsConfigFields {
    public EnchantedBookSoulSpeedConfig(){
        super("enchanted_book_soul_speed",
                true,
                Material.BOOK,
                "&5Elite Soul Speed Enchanted Book",
                new ArrayList<>(List.of("&2Used to enchant items at the enchanter!")));
        setEnchantments(new ArrayList<>(List.of("SOUL_SPEED,1", "ENCHANTED_SOURCE,1")));
        setItemType(CustomItem.ItemType.UNIQUE);
        setScalability(CustomItem.Scalability.FIXED);
        setSoulbound(false);
    }
}
