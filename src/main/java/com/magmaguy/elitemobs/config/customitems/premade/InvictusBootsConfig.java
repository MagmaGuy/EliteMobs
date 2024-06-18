package com.magmaguy.elitemobs.config.customitems.premade;

import com.magmaguy.elitemobs.config.customitems.CustomItemsConfigFields;
import com.magmaguy.elitemobs.items.customitems.CustomItem;
import com.magmaguy.elitemobs.versionnotifier.VersionChecker;
import org.bukkit.Material;

import java.util.Arrays;
import java.util.List;

public class InvictusBootsConfig extends CustomItemsConfigFields {
    public InvictusBootsConfig() {
        super("invictus_boots", true, Material.DIAMOND_BOOTS, "&4Invictus Boots", Arrays.asList("&2Awarded to the champions of the", "&2Wood League Arena!"));
        if (!VersionChecker.serverVersionOlderThan(16, 0))
            setMaterial(Material.NETHERITE_BOOTS);
        setEnchantments(Arrays.asList("PROTECTION,5", "PROTECTION_EXPLOSIONS,4", "PROTECTION_PROJECTILE,4", "MENDING,1", "UNBREAKING,5"));
        setPotionEffects(List.of("SPEED,0,self,continuous"));
        setItemType(CustomItem.ItemType.UNIQUE);
        setLevel(50);
    }
}
