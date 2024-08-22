package com.magmaguy.elitemobs.config.customitems.premade;

import com.magmaguy.elitemobs.config.customitems.CustomItemsConfigFields;
import com.magmaguy.elitemobs.items.customitems.CustomItem;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;

public class UnbindScrollConfig extends CustomItemsConfigFields {
    public UnbindScrollConfig() {
        super("unbind_scroll",
                true,
                Material.PAPER,
                "&5Unbind Scroll",
                new ArrayList<>(List.of("&5Allows users to unbind one",
                        "&5soulbound item at an anvil!",
                        "&5Use wisely!")));
        setEnchantments(List.of("UNBIND,1"));
        setItemType(CustomItem.ItemType.UNIQUE);
    }
}
