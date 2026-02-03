package com.magmaguy.elitemobs.config.customitems.premade;

import com.magmaguy.elitemobs.config.customitems.CustomItemsConfigFields;
import com.magmaguy.elitemobs.items.customitems.CustomItem;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;

public class DepthsSeekerConfig extends CustomItemsConfigFields {
    public DepthsSeekerConfig() {
        super("depths_seeker",
                true,
                Material.FISHING_ROD,
                "<g:#1E90FF:#00CED1>Depths Seeker</g>",
                new ArrayList<>(List.of("&9Come from depths immeasurable", "&9and looted from monster most vile,", "&9there is no telling what horrors", "&9this fishing rod has seen.")));
        setEnchantments(new ArrayList<>(List.of("LURE,3", "LUCK_OF_THE_SEA,3", "UNBREAKING,10", "FIRE_ASPECT,1", "VANISHING_CURSE,1")));
        setPotionEffects(new ArrayList<>(List.of("WATER_BREATHING,0,self,continuous", "LUCK,0,self,continuous")));
        setItemType(CustomItem.ItemType.UNIQUE);
    }
}
