package com.magmaguy.elitemobs.config.customitems.premade;

import com.magmaguy.elitemobs.config.customitems.CustomItemsConfigFields;
import com.magmaguy.elitemobs.items.customitems.CustomItem;
import org.bukkit.Material;

import java.util.Arrays;

public class RodOfTheDepthsConfig extends CustomItemsConfigFields {
    public RodOfTheDepthsConfig() {
        super("rod_of_the_depths",
                true,
                Material.FISHING_ROD,
                "&3Rod of the Depths",
                Arrays.asList("&3You want to hear about where", "&3I got this from? Sit down lad,", "&3I've got a tale for ye..."));
        setEnchantments(Arrays.asList("LURE,3", "LUCK,1", "DURABILITY,6", "VANISHING_CURSE,1"));
        setPotionEffects(Arrays.asList("INVISIBILITY,0,self,onHit", "SLOW,0,self,onHit", "BLINDNESS,0,self,onHit"));
        setItemType(CustomItem.ItemType.UNIQUE);
    }
}
