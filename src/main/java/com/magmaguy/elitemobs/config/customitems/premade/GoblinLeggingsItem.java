package com.magmaguy.elitemobs.config.customitems.premade;

import com.magmaguy.elitemobs.config.customitems.CustomItemsConfigFields;
import com.magmaguy.elitemobs.items.customitems.CustomItem;
import com.magmaguy.elitemobs.versionnotifier.VersionChecker;
import org.bukkit.Material;

import java.util.Arrays;
import java.util.List;

public class GoblinLeggingsItem extends CustomItemsConfigFields {
    public GoblinLeggingsItem() {
        super("goblin_leggings",
                true,
                Material.GOLDEN_LEGGINGS,
                "&8Goblin Leggings",
                List.of("&8A treasure among goblins!"));
        if (!VersionChecker.serverVersionOlderThan(16, 0))
            setMaterial(Material.NETHERITE_LEGGINGS);
        setEnchantments(Arrays.asList("PROTECTION,1", "PROTECTION_EXPLOSIONS,1", "PROTECTION_PROJECTILE,1", "UNBREAKING,1"));
        setPotionEffects(List.of("JUMP,0,self,continuous"));
        setItemType(CustomItem.ItemType.UNIQUE);
    }
}
