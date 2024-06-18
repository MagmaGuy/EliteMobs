package com.magmaguy.elitemobs.config.customitems.premade;

import com.magmaguy.elitemobs.config.customitems.CustomItemsConfigFields;
import com.magmaguy.elitemobs.items.customitems.CustomItem;
import com.magmaguy.elitemobs.versionnotifier.VersionChecker;
import org.bukkit.Material;

import java.util.Arrays;
import java.util.List;

public class GoblinChestplateItem extends CustomItemsConfigFields {
    public GoblinChestplateItem() {
        super("goblin_chestplate",
                true,
                Material.GOLDEN_CHESTPLATE,
                "&8Goblin Chestplate",
                List.of("&8A treasure among goblins!"));
        if (!VersionChecker.serverVersionOlderThan(16, 0))
            setMaterial(Material.NETHERITE_CHESTPLATE);
        setEnchantments(Arrays.asList("PROTECTION,1", "PROTECTION_EXPLOSIONS,1", "PROTECTION_PROJECTILE,1", "UNBREAKING,1"));
        setPotionEffects(List.of("SATURATION,0,self,continuous"));
        setItemType(CustomItem.ItemType.UNIQUE);
    }
}
