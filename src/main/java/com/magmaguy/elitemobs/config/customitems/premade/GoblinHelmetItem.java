package com.magmaguy.elitemobs.config.customitems.premade;

import com.magmaguy.elitemobs.config.customitems.CustomItemsConfigFields;
import com.magmaguy.elitemobs.items.customitems.CustomItem;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;

public class GoblinHelmetItem extends CustomItemsConfigFields {
    public GoblinHelmetItem() {
        super("goblin_helmet",
                true,
                Material.NETHERITE_HELMET,
                "&8Goblin Helmet",
                List.of("&8A treasure among goblins!"));
        setEnchantments(new ArrayList<>(List.of("PROTECTION,1", "BLAST_PROTECTION,1", "PROJECTILE_PROTECTION,1", "UNBREAKING,1")));
        setPotionEffects(List.of("NIGHT_VISION,0,self,continuous"));
        setItemType(CustomItem.ItemType.UNIQUE);
    }
}
