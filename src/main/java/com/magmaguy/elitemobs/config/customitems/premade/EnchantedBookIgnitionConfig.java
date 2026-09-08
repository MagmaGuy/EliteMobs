package com.magmaguy.elitemobs.config.customitems.premade;

import com.magmaguy.elitemobs.config.customitems.CustomItemsConfigFields;
import com.magmaguy.elitemobs.items.customitems.CustomItem;
import org.bukkit.Material;
import java.util.ArrayList;
import java.util.List;

public final class EnchantedBookIgnitionConfig extends CustomItemsConfigFields {
    public EnchantedBookIgnitionConfig() {
        super("enchanted_book_ignition", true, Material.BOOK, "&5Elite Ignition Enchanted Book",
                new ArrayList<>(List.of("&2Used at the enchanter.", "&7Staves only. Ignites enemies on impact.")));
        setEnchantments(new ArrayList<>(List.of("IGNITION,1", "ENCHANTED_SOURCE,1")));
        setItemType(CustomItem.ItemType.UNIQUE);
        setScalability(CustomItem.Scalability.FIXED);
        setSoulbound(false);
    }
}
