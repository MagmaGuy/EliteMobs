package com.magmaguy.elitemobs.config.customitems.premade;

import com.magmaguy.elitemobs.config.customitems.CustomItemsConfigFields;
import com.magmaguy.elitemobs.items.customitems.CustomItem;
import org.bukkit.Material;

import java.util.Arrays;

public class SummonWolfScrollConfig extends CustomItemsConfigFields {
    public SummonWolfScrollConfig() {
        super("summon_wolf_scroll",
                true,
                Material.PAPER,
                "&6Summon Wolf Scroll",
                Arrays.asList("&aSummons a good boy", "&ato help you bite Elites!", "&aMake sure you pet it!"));
        setEnchantments(Arrays.asList("SUMMON_WOLF,1"));
        setItemType(CustomItem.ItemType.UNIQUE);
    }
}
