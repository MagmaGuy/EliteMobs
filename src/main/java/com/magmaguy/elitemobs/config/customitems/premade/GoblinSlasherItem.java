package com.magmaguy.elitemobs.config.customitems.premade;

import com.magmaguy.elitemobs.config.customitems.CustomItemsConfigFields;
import com.magmaguy.elitemobs.items.customitems.CustomItem;
import org.bukkit.Material;

import java.util.Arrays;
import java.util.List;

public class GoblinSlasherItem extends CustomItemsConfigFields {
    public GoblinSlasherItem() {
        super("goblin_slasher",
                true,
                Material.NETHERITE_SWORD,
                "&8Goblin Slasher",
                List.of("&8A treasure among goblins!"));
        setEnchantments(Arrays.asList("SHARPNESS,1", "SMITE,1", "UNBREAKING,1", "KNOCKBACK,1", "LOOTING,5"));
        setPotionEffects(List.of("FAST_DIGGING,0,self,onHit"));
        setItemType(CustomItem.ItemType.UNIQUE);
    }
}
