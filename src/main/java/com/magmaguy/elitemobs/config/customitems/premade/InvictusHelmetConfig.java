package com.magmaguy.elitemobs.config.customitems.premade;

import com.magmaguy.elitemobs.config.customitems.CustomItemsConfigFields;
import com.magmaguy.elitemobs.items.customitems.CustomItem;
import com.magmaguy.elitemobs.utils.VersionChecker;
import org.bukkit.Material;

import java.util.Arrays;

public class InvictusHelmetConfig extends CustomItemsConfigFields {
    public InvictusHelmetConfig() {
        super("invictus_helmet", true, Material.DIAMOND_HELMET, "&4Invictus Helmet", Arrays.asList("&2Awarded to the champions of the", "&2Wood League Arena!"));
        if (!VersionChecker.serverVersionOlderThan(16, 0))
            setMaterial(Material.NETHERITE_HELMET);
        setEnchantments(Arrays.asList("PROTECTION_ENVIRONMENTAL,5", "PROTECTION_EXPLOSIONS,4", "PROTECTION_PROJECTILE,4", "MENDING,1", "DURABILITY,5"));
        setPotionEffects(Arrays.asList("NIGHT_VISION,0,self,continuous"));
        setItemType(CustomItem.ItemType.UNIQUE);
        setLevel(50);
    }
}
