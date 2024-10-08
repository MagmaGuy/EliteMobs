package com.magmaguy.elitemobs.config.customitems.premade;

import com.magmaguy.elitemobs.config.customitems.CustomItemsConfigFields;
import com.magmaguy.elitemobs.items.customitems.CustomItem;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;

public class GoblinPokerItem extends CustomItemsConfigFields {
    public GoblinPokerItem() {
        super("goblin_poker",
                true,
                Material.TRIDENT,
                "&8Goblin Poker",
                List.of("&8A treasure among goblins!"));
        setEnchantments(new ArrayList<>(List.of("SHARPNESS,1", "SMITE,1", "UNBREAKING,1", "KNOCKBACK,1", "LOOTING,5")));
        setPotionEffects(List.of("WITHER,0,target,onHit"));
        setItemType(CustomItem.ItemType.UNIQUE);
    }
}
