package com.magmaguy.elitemobs.config.customitems.premade;

import com.magmaguy.elitemobs.config.customitems.CustomItemsConfigFields;
import com.magmaguy.elitemobs.items.customitems.CustomItem;
import org.bukkit.Material;

import java.util.Arrays;

public class GoblinLeggingsItem  extends CustomItemsConfigFields {
    public GoblinLeggingsItem(){
        super("goblin_leggings",
                true,
                Material.NETHERITE_LEGGINGS,
                "&8Goblin Leggings",
                Arrays.asList("&8A treasure among goblins!"));
        setEnchantments(Arrays.asList("PROTECTION_ENVIRONMENTAL,1", "PROTECTION_EXPLOSIONS,1", "PROTECTION_PROJECTILE,1", "DURABILITY,1"));
        setPotionEffects(Arrays.asList("JUMP,0,self,continuous"));
        setItemType(CustomItem.ItemType.unique);
    }
}
