package com.magmaguy.elitemobs.config.customitems.premade;

import com.magmaguy.elitemobs.config.customitems.CustomItemsConfigFields;
import com.magmaguy.elitemobs.items.customitems.CustomItem;
import org.bukkit.Material;

import java.util.Arrays;

public class GoblinSlasherItem extends CustomItemsConfigFields {
    public GoblinSlasherItem(){
        super("goblin_slasher",
                true,
                Material.NETHERITE_SWORD,
                "&8Goblin Slasher",
                Arrays.asList("&8A treasure among goblins!"));
        setEnchantments(Arrays.asList("DAMAGE_ALL,1", "DAMAGE_UNDEAD,1", "DURABILITY,1", "KNOCKBACK,1", "LOOT_BONUS_MOBS,5"));
        setPotionEffects(Arrays.asList("FAST_DIGGING,0,self,onHit"));
        setItemType(CustomItem.ItemType.unique);
    }
}
