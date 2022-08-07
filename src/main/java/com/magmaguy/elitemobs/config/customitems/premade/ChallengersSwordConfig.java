package com.magmaguy.elitemobs.config.customitems.premade;

import com.magmaguy.elitemobs.config.customitems.CustomItemsConfigFields;
import com.magmaguy.elitemobs.items.customitems.CustomItem;
import org.bukkit.Material;

import java.util.Arrays;

public class ChallengersSwordConfig extends CustomItemsConfigFields {
    public ChallengersSwordConfig(){
        super("challengers_sword", true, Material.DIAMOND_SWORD, "&cChallenger's Sword", Arrays.asList("&2Awarded to those who challenge the", "&2Wood League Arena!"));
        setEnchantments(Arrays.asList("DAMAGE_ALL,5", "KNOCKBACK,2", "MENDING,1", "LIGHTNING,3", "SWEEPING_EDGE,1", "DURABILITY,5"));
        setItemType(CustomItem.ItemType.UNIQUE);
        setLevel(40);
    }
}
