package com.magmaguy.elitemobs.config.customitems.premade;

import com.magmaguy.elitemobs.config.customitems.CustomItemsConfigFields;
import com.magmaguy.elitemobs.items.customitems.CustomItem;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;

public class EnchantedBookDamageUndeadConfig extends CustomItemsConfigFields {
    public EnchantedBookDamageUndeadConfig(){
        super("enchanted_book_damage_undead",
                true,
                Material.BOOK,
                "&5Elite Smite Enchanted Book",
                new ArrayList<>(List.of("&2Used to enchant items at the enchanter!")));
        setEnchantments(new ArrayList<>(List.of("DAMAGE_UNDEAD,1", "ENCHANTED_SOURCE,1")));
        setItemType(CustomItem.ItemType.UNIQUE);
        setScalability(CustomItem.Scalability.FIXED);
        setSoulbound(false);
    }
}
