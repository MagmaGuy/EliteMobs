package com.magmaguy.elitemobs.config.customitems.premade;

import com.magmaguy.elitemobs.config.customitems.CustomItemsConfigFields;
import com.magmaguy.elitemobs.items.customitems.CustomItem;
import org.bukkit.Material;

import java.util.Arrays;
import java.util.List;

public class GoblinCleaverItem extends CustomItemsConfigFields {
    public GoblinCleaverItem() {
        super("goblin_cleaver",
                true,
                Material.NETHERITE_AXE,
                "&8Goblin Cleaver",
                List.of("&8A treasure among goblins!"));
        setEnchantments(Arrays.asList("SHARPNESS,1", "SMITE,1", "UNBREAKING,1", "KNOCKBACK,1", "LOOTING,5"));
        setPotionEffects(List.of("POISON,0,target,onHit"));
        setItemType(CustomItem.ItemType.UNIQUE);
    }
}
