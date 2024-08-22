package com.magmaguy.elitemobs.config.customitems.premade;

import com.magmaguy.elitemobs.config.customitems.CustomItemsConfigFields;
import com.magmaguy.elitemobs.items.customitems.CustomItem;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;

public class RodOfTheDepthsConfig extends CustomItemsConfigFields {
    public RodOfTheDepthsConfig() {
        super("rod_of_the_depths",
                true,
                Material.FISHING_ROD,
                "&3Rod of the Depths",
                new ArrayList<>(List.of("&3You want to hear about where", "&3I got this from? Sit down lad,", "&3I've got a tale for ye...")));
        setEnchantments(new ArrayList<>(List.of("LURE,3", "LUCK_OF_THE_SEA,1", "UNBREAKING,6", "VANISHING_CURSE,1")));
        setPotionEffects(new ArrayList<>(List.of("INVISIBILITY,0,self,onHit", "SLOW,0,self,onHit", "BLINDNESS,0,self,onHit")));
        setItemType(CustomItem.ItemType.UNIQUE);
    }
}
