package com.magmaguy.elitemobs.config.customitems.premade;

import com.magmaguy.elitemobs.config.customitems.CustomItemsConfigFields;
import com.magmaguy.elitemobs.items.customitems.CustomItem;
import org.bukkit.Material;

import java.util.Arrays;
import java.util.List;

public class GoblinBootsItem extends CustomItemsConfigFields {
    public GoblinBootsItem() {
        super("goblin_boots",
                true,
                Material.GOLDEN_BOOTS,
                "&8Goblin Boots",
                List.of("&8A treasure among goblins!"));
        setMaterial(Material.NETHERITE_BOOTS);
        setEnchantments(Arrays.asList("PROTECTION,1", "BLAST_PROTECTION,1", "PROJECTILE_PROTECTION,1", "UNBREAKING,1"));
        setPotionEffects(List.of("SPEED,0,self,continuous"));
        setItemType(CustomItem.ItemType.UNIQUE);
    }
}
