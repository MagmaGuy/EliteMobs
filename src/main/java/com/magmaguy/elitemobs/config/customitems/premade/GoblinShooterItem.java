package com.magmaguy.elitemobs.config.customitems.premade;

import com.magmaguy.elitemobs.config.customitems.CustomItemsConfigFields;
import com.magmaguy.elitemobs.items.customitems.CustomItem;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;

public class GoblinShooterItem extends CustomItemsConfigFields {
    public GoblinShooterItem() {
        super("goblin_shooter",
                true,
                Material.BOW,
                "&8Goblin Shooter",
                List.of("&8A treasure among goblins!"));
        setEnchantments(new ArrayList<>(List.of("POWER,1", "UNBREAKING,1", "INFINITY,1", "FLAME,1")));
        setPotionEffects(List.of("SPEED,2,self,onHit"));
        setItemType(CustomItem.ItemType.UNIQUE);
    }
}
