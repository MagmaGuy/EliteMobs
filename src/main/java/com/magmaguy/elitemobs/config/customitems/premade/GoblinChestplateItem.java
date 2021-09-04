package com.magmaguy.elitemobs.config.customitems.premade;

import com.magmaguy.elitemobs.config.customitems.CustomItemsConfigFields;
import com.magmaguy.elitemobs.items.customitems.CustomItem;
import org.bukkit.Material;

import java.util.Arrays;

public class GoblinChestplateItem extends CustomItemsConfigFields {
    public GoblinChestplateItem(){
        super("goblin_chestplate",
                true,
                Material.NETHERITE_CHESTPLATE,
                "&8Goblin Chestplate",
                Arrays.asList("&8A treasure among goblins!"));
        setEnchantments(Arrays.asList("PROTECTION_ENVIRONMENTAL,1", "PROTECTION_EXPLOSIONS,1", "PROTECTION_PROJECTILE,1", "DURABILITY,1"));
        setPotionEffects(Arrays.asList("SATURATION,0,self,continuous"));
        setItemType(CustomItem.ItemType.UNIQUE);
    }
}
