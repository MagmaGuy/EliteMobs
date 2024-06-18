package com.magmaguy.elitemobs.config.customitems.premade;

import com.magmaguy.elitemobs.config.customitems.CustomItemsConfigFields;
import com.magmaguy.elitemobs.items.customitems.CustomItem;
import org.bukkit.Material;

import java.util.Arrays;

public class DepthsSeekerConfig extends CustomItemsConfigFields {
    public DepthsSeekerConfig() {
        super("depths_seeker",
                true,
                Material.FISHING_ROD,
                "&2Depths seeker",
                Arrays.asList("&9Come from depths immeasurable", "&9and looted from monster most vile,", "&9there is no telling what horrors", "&9this fishing rod has seen."));
        setEnchantments(Arrays.asList("LURE,3", "LUCK,3", "UNBREAKING,10", "FIRE_ASPECT,1", "VANISHING_CURSE,1"));
        setPotionEffects(Arrays.asList("WATER_BREATHING,0,self,continuous", "LUCK,0,self,continuous"));
        setItemType(CustomItem.ItemType.UNIQUE);
    }
}
