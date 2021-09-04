package com.magmaguy.elitemobs.config.customitems.premade;

import com.magmaguy.elitemobs.config.customitems.CustomItemsConfigFields;
import com.magmaguy.elitemobs.items.customitems.CustomItem;
import org.bukkit.Material;

import java.util.Arrays;

public class GoblinCleaverItem extends CustomItemsConfigFields {
    public GoblinCleaverItem(){
        super("goblin_cleaver",
                true,
                Material.NETHERITE_AXE,
                "&8Goblin Cleaver",
                Arrays.asList("&8A treasure among goblins!"));
        setEnchantments(Arrays.asList("DAMAGE_ALL,1", "DAMAGE_UNDEAD,1", "DURABILITY,1", "KNOCKBACK,1", "LOOT_BONUS_MOBS,5"));
        setPotionEffects(Arrays.asList("POISON,0,target,onHit"));
        setItemType(CustomItem.ItemType.UNIQUE);
    }
}
