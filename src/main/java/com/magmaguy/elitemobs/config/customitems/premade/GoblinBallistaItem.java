package com.magmaguy.elitemobs.config.customitems.premade;

import com.magmaguy.elitemobs.config.customitems.CustomItemsConfigFields;
import com.magmaguy.elitemobs.items.customitems.CustomItem;
import org.bukkit.Material;

import java.util.Arrays;

public class GoblinBallistaItem extends CustomItemsConfigFields {
    public GoblinBallistaItem(){
        super("goblin_ballista",
                true,
                Material.CROSSBOW,
                "&8Goblin Ballista",
                Arrays.asList("&8A treasure among goblins!"));
        setEnchantments(Arrays.asList("ARROW_DAMAGE,1", "DURABILITY,1", "QUICK_CHARGE,3", "MULTISHOT,1"));
        setPotionEffects(Arrays.asList("HEAL,0,self,onHit"));
        setItemType(CustomItem.ItemType.UNIQUE);
    }
}
