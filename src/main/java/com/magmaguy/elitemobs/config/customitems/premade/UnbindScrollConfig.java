package com.magmaguy.elitemobs.config.customitems.premade;

import com.magmaguy.elitemobs.config.customitems.CustomItemsConfigFields;
import com.magmaguy.elitemobs.items.customitems.CustomItem;
import org.bukkit.Material;

import java.util.Arrays;

public class UnbindScrollConfig extends CustomItemsConfigFields {
    public UnbindScrollConfig() {
        super("unbind_scroll",
                true,
                Material.PAPER,
                "&5Unbind Scroll",
                Arrays.asList("&5Allows users to unbind one",
                        "&5soulbound item at an anvil!",
                        "&5Use wisely!"));
        setEnchantments(Arrays.asList("UNBIND,1"));
        setItemType(CustomItem.ItemType.UNIQUE);
    }
}
