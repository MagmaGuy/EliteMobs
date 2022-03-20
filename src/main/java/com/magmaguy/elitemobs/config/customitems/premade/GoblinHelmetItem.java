package com.magmaguy.elitemobs.config.customitems.premade;

import com.magmaguy.elitemobs.config.customitems.CustomItemsConfigFields;
import com.magmaguy.elitemobs.items.customitems.CustomItem;
import com.magmaguy.elitemobs.utils.VersionChecker;
import org.bukkit.Material;

import java.util.Arrays;

public class GoblinHelmetItem extends CustomItemsConfigFields {
    public GoblinHelmetItem() {
        super("goblin_helmet",
                true,
                Material.GOLDEN_HELMET,
                "&8Goblin Helmet",
                Arrays.asList("&8A treasure among goblins!"));
        if (!VersionChecker.serverVersionOlderThan(16, 0))
            setMaterial(Material.NETHERITE_HELMET);
        setEnchantments(Arrays.asList("PROTECTION_ENVIRONMENTAL,1", "PROTECTION_EXPLOSIONS,1", "PROTECTION_PROJECTILE,1", "DURABILITY,1"));
        setPotionEffects(Arrays.asList("NIGHT_VISION,0,self,continuous"));
        setItemType(CustomItem.ItemType.UNIQUE);
    }
}
