package com.magmaguy.elitemobs.config.customitems.premade;

import com.magmaguy.elitemobs.config.customitems.CustomItemsConfigFields;
import com.magmaguy.elitemobs.items.customitems.CustomItem;
import org.bukkit.Material;
import java.util.ArrayList;
import java.util.List;

public final class EnchantedBookBlastRadiusConfig extends CustomItemsConfigFields {
    public EnchantedBookBlastRadiusConfig() {
        super("enchanted_book_blast_radius", true, Material.ENCHANTED_BOOK, "&5Elite Blast Radius Enchanted Book",
                new ArrayList<>(List.of("&2Used at the enchanter.", "&7Staves only. Increases explosion radius.")));
        setEnchantments(new ArrayList<>(List.of("freeminecraftmodels:blast_radius,1")));
        setItemType(CustomItem.ItemType.UNIQUE);
        setScalability(CustomItem.Scalability.FIXED);
        setSoulbound(false);
    }
}
