package com.magmaguy.elitemobs.config.customitems.premade;

import com.magmaguy.elitemobs.config.customitems.CustomItemsConfigFields;
import com.magmaguy.elitemobs.items.customitems.CustomItem;
import org.bukkit.Material;

import java.util.Arrays;

public class GoblinBootsItem extends CustomItemsConfigFields {
    public GoblinBootsItem(){
        super("goblin_boots",
                true,
                Material.NETHERITE_BOOTS,
                "&8Goblin Boots",
                Arrays.asList("&8A treasure among goblins!"));
        setEnchantments(Arrays.asList("PROTECTION_ENVIRONMENTAL,1", "PROTECTION_EXPLOSIONS,1", "PROTECTION_PROJECTILE,1", "DURABILITY,1"));
        setPotionEffects(Arrays.asList("SPEED,0,self,continuous"));
        setItemType(CustomItem.ItemType.unique);
    }
}
