package com.magmaguy.elitemobs.config.customitems.premade;

import com.magmaguy.elitemobs.config.customitems.CustomItemsConfigFields;
import com.magmaguy.elitemobs.items.customitems.CustomItem;
import org.bukkit.Material;

import java.util.Arrays;
import java.util.List;

public class GoblinShooterItem extends CustomItemsConfigFields {
    public GoblinShooterItem() {
        super("goblin_shooter",
                true,
                Material.BOW,
                "&8Goblin Shooter",
                List.of("&8A treasure among goblins!"));
        setEnchantments(Arrays.asList("ARROW_DAMAGE,1", "DURABILITY,1", "ARROW_INFINITE,1", "ARROW_FIRE,1"));
        setPotionEffects(List.of("SPEED,2,self,onHit"));
        setItemType(CustomItem.ItemType.UNIQUE);
    }
}
