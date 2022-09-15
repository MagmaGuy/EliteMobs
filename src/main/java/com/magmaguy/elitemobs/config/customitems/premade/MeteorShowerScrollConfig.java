package com.magmaguy.elitemobs.config.customitems.premade;

import com.magmaguy.elitemobs.config.customitems.CustomItemsConfigFields;
import com.magmaguy.elitemobs.items.customitems.CustomItem;
import org.bukkit.Material;

import java.util.Arrays;
import java.util.List;

public class MeteorShowerScrollConfig extends CustomItemsConfigFields {
    public MeteorShowerScrollConfig() {
        super("meteor_shower_scroll",
                true,
                Material.PAPER,
                "&7Meteor Shower Scroll",
                Arrays.asList("&4Call forth destruction.", "&4Single-use."));
        setEnchantments(List.of("METEOR_SHOWER,1"));
        setItemType(CustomItem.ItemType.UNIQUE);
    }
}
