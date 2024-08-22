package com.magmaguy.elitemobs.config.customitems.premade;

import com.magmaguy.elitemobs.config.customitems.CustomItemsConfigFields;
import com.magmaguy.elitemobs.items.customitems.CustomItem;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;

public class GoblinLeggingsItem extends CustomItemsConfigFields {
    public GoblinLeggingsItem() {
        super("goblin_leggings",
                true,
                Material.NETHERITE_LEGGINGS,
                "&8Goblin Leggings",
                List.of("&8A treasure among goblins!"));
        setEnchantments(new ArrayList<>(List.of("PROTECTION,1", "BLAST_PROTECTION,1", "PROJECTILE_PROTECTION,1", "UNBREAKING,1")));
        setPotionEffects(List.of("JUMP,0,self,continuous"));
        setItemType(CustomItem.ItemType.UNIQUE);
    }
}
