package com.magmaguy.elitemobs.config.customitems;

import com.magmaguy.elitemobs.items.customitems.CustomItem;
import org.bukkit.Material;

import java.util.List;

/** Ordinary fixed quest items with authored hard-mode boss-equivalent enchantments. */
public abstract class AdventurerRewardItemConfig extends CustomItemsConfigFields {
    protected AdventurerRewardItemConfig(String item, Material material, List<String> enchantments) {
        super("ag_adventurer_" + item.toLowerCase(java.util.Locale.ROOT), material != null, material,
                "&aAdventurer's " + item, List.of("&7Awarded by Casus for completing your first class trial."));
        setLevel(1);
        setScalability(CustomItem.Scalability.FIXED);
        setItemType(CustomItem.ItemType.UNIQUE);
        setEnchantments(enchantments);
    }
}
