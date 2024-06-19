package com.magmaguy.elitemobs.config.customitems.premade;

import com.magmaguy.elitemobs.config.customitems.CustomItemsConfigFields;
import com.magmaguy.elitemobs.items.customitems.CustomItem;
import org.bukkit.Material;

import java.util.Arrays;
import java.util.List;

public class InvictusShovelConfig extends CustomItemsConfigFields {
    public InvictusShovelConfig() {
        super("invictus_shovel", true, Material.NETHERITE_SHOVEL, "&4Invictus Shovel", Arrays.asList("&2Awarded to the champions of the", "&2Wood League Arena!"));
        setEnchantments(Arrays.asList("EFFICIENCY,6", "DRILLING,3", "UNBREAKING,5"));
        setPotionEffects(List.of("FAST_DIGGING,0,self,continuous"));
        setItemType(CustomItem.ItemType.UNIQUE);
    }
}
